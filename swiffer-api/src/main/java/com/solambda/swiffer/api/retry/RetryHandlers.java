package com.solambda.swiffer.api.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.model.ActivityTaskScheduledEventAttributes;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.Control;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.context.ActivityTaskFailedContext;
import com.solambda.swiffer.api.internal.context.ActivityTaskTimedOutContext;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskContext;
import com.solambda.swiffer.api.internal.decisions.WorkflowEvent;

/**
 * Class for default handler to support automatic retry policy.
 *
 * @see RetryPolicy
 */
public final class RetryHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandlers.class);

    private final RetryPolicy retryPolicy;

    /**
     * Creates new {@link RetryHandlers} class with specified retry policy.
     *
     * @param retryPolicy global retry policy for failed and timed out activities which doesn't have handlers
     */
    public RetryHandlers(RetryPolicy retryPolicy) {
        Preconditions.checkNotNull(retryPolicy, "Retry policy must be specified.");
        this.retryPolicy = retryPolicy;
    }

    /**
     * Handler for failed activity.
     * Activity will be retried based on {@link #retryPolicy}.
     *
     * @param scheduledEventId id of the ActivityTaskScheduled event that was recorded when activity task that failed was scheduled
     * @param decideTo         the {@link Decisions} object
     * @param context          the failed activity context
     */
    public void onFailure(Long scheduledEventId, Decisions decideTo, ActivityTaskFailedContext context) {
        String activityName = context.activityType().name();
        LOGGER.debug("Activity [{}] has failed and will be retried with the retry policy: {}.", activityName, retryPolicy);
        retryActivity(scheduledEventId, activityName, decideTo, context);
    }

    /**
     * Handler for timed out activity.
     * Activity will be retried based on {@link #retryPolicy}.
     *
     * @param scheduledEventId id of the ActivityTaskScheduled event that was recorded when activity task that failed was scheduled
     * @param decideTo         the {@link Decisions} object
     * @param context          the timed out activity context
     */
    public void onTimeout(Long scheduledEventId, Decisions decideTo, ActivityTaskTimedOutContext context) {
        String activityName = context.activityType().name();
        LOGGER.debug("Activity [{}] has timed out and will be retried with the retry policy: {}.", activityName, retryPolicy);
        retryActivity(scheduledEventId, activityName, decideTo, context);
    }

    public void onTimer(@Control RetryControl control, Decisions decideTo, DecisionTaskContext context) {
        WorkflowEvent failedActivity = context.history().getEventById(control.getScheduledEventId());
        ActivityTaskScheduledEventAttributes activityAttributes = failedActivity.getActivityTaskScheduledEventAttributes();

        int retries = context.getMarkerDetails(control.getMarkerName(), Integer.class).orElse(0);
        LOGGER.debug("Attempt to execute Activity after {} failed retries. Initial activity attributes: {}.", retries, activityAttributes);
        decideTo.scheduleActivityTask(activityAttributes)
                .recordMarker(control.getMarkerName(), ++retries);
    }

    /**
     * Returns retry policy for the default handlers.
     *
     * @return retry policy
     */
    RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    /**
     * Returns {@code true} if Activities should be retried by default.
     *
     * @return {@code true} if Activity should be retried if failed or timed out
     */
    public boolean shouldRetry() {
        return !(retryPolicy instanceof NoRetryPolicy);
    }

    private void retryActivity(Long scheduledEventId, String activityName, Decisions decideTo, DecisionTaskContext context){
        decideTo.retryActivity(scheduledEventId, activityName, context, retryPolicy);
    }
}
