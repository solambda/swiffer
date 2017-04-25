package com.solambda.swiffer.api;

import java.time.Duration;

import com.amazonaws.services.simpleworkflow.model.ActivityTaskScheduledEventAttributes;
import com.solambda.swiffer.api.internal.context.ActivityTaskFailedContext;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskContext;
import com.solambda.swiffer.api.retry.RetryPolicy;

/**
 * A convenient builder of decisions.
 * <p>
 * It must be passed as a parameter of any event handler method.
 * <p>
 */
@EventHandlerCommonParameter
public interface Decisions {
	/**
	 * Add a "schedule activity task" decision.
	 * <p>
	 * The activity id will be computed by concatenating the activity name, the
	 * version and a timestamp.
	 *
	 * @param activityType
	 *            class annotated with {@link ActivityType}
	 * @param input
	 *            the input to provide to the activity task
	 * @return this decision object
	 */
	Decisions scheduleActivityTask(Class<?> activityType, Object input);

	/**
	 * Add a "schedule activity task" decision. *
	 * <p>
	 * The activity id will be computed by concatenating the activity name, the
	 * version and a timestamp.
	 *
	 * @param activityType
	 *            class annotated with {@link ActivityType}
	 * @param options
	 *            the {@link ActivityOptions}
	 * @return this decision object
	 */
	Decisions scheduleActivityTask(Class<?> activityType, ActivityOptions options);

	/**
	 * Add a "schedule activity task" decision. *
	 * <p>
	 * The activity id will be computed by concatenating the activity name, the
	 * version and a timestamp.
	 *
	 * @param activityType
	 *            class annotated with {@link ActivityType}
	 * @param input
	 *            the input to provide to the activity task
	 * @param options
	 *            the {@link ActivityOptions}
	 * @return this decision object
	 */
	Decisions scheduleActivityTask(Class<?> activityType, Object input, ActivityOptions options);

	/**
	 * Add a "schedule activity task" decision.
	 * <p>
	 *
	 * @param activityType
	 *            class annotated with {@link ActivityType}
	 * @param input
	 *            the input to provide to the activity task
	 * @param activityId
	 *            the id to give to this activity task, in order to cancel it
	 *            later
	 * @param options
	 *            the {@link ActivityOptions}
	 * @return this decision object
	 */
	Decisions scheduleActivityTask(final Class<?> activityType, Object input, String activityId,
			ActivityOptions options);

	/**
	 * Add a "complete workflow execution" decision.
	 *
	 * @return this decision object
	 */
	Decisions completeWorkflow();

	/**
	 * Add a "complete workflow execution" decision.
	 *
	 * @param result
	 *            The result of the workflow execution
	 * @return this decision object
	 */
	Decisions completeWorkflow(Object result);

	/**
	 * Add a "start timer" decision.
	 *
	 * @param timerId
	 *            the id of the timer, must not be equals to an existing started
	 *            timer that is not fired or cancelled
	 * @param duration
	 *            duration of the timer before firing
	 * @return this decisions object
	 */
	Decisions startTimer(String timerId, Duration duration);

	/**
	 * Add a "start timer" decision.
	 *
	 * @param timerId
	 *            the id of the timer, must not be equals to an existing started
	 *            timer that is not fired or cancelled
	 * @param duration
	 *            duration of the timer before firing
	 * @param control
	 *            an optional object to be set in the timer attributes
	 * @return this decisions object
	 */
	Decisions startTimer(String timerId, Duration duration, Object control);

	/**
	 * Add a "cancel timer" decision.
	 *
	 * @param timerId
	 *            the id of the timer, must be equals to an existing started
	 *            timer that is not fired or cancelled
	 * @return this decisions object
	 */
	Decisions cancelTimer(String timerId);

	/**
	 * Add a "fail workflow execution" decision.
	 *
	 * @param reason  The reason for the workflow failure
	 * @param details the details of workflow failure
	 * @return this decision object
	 */
	Decisions failWorkflow(String reason, String details);

    /**
     * Add a "record marker" decision.
     *
     * @param markerName marker name
     * @param details    an optional object for recorded marker
     * @return this decision object
     */
    Decisions recordMarker(String markerName, Object details);

    /**
     * Add a "record marker" decision.
     *
     * @param markerName marker name
     * @return this decision object
     */
    Decisions recordMarker(String markerName);

	/**
	 * Automatically retries failed activity specified by {@link ActivityTaskFailedContext} with default retry policy.
	 *
	 * @param scheduledEventId id of the ActivityTaskScheduled event that was recorded when activity task that failed was scheduled
	 * @param context          the context of the failed activity task
	 * @return this {@link Decisions} object
	 */
	Decisions retryActivity(Long scheduledEventId, ActivityTaskFailedContext context);

	/**
	 * Automatically retries failed activity from {@link ActivityTaskFailedContext} with specified {@code retryPolicy}.
	 *
	 * @param scheduledEventId id of the ActivityTaskScheduled event that was recorded when activity task that failed was scheduled
	 * @param context          the {@link DecisionTaskContext}
	 * @param retryPolicy      one of the {@link RetryPolicy} which should be used to retry the activity
	 * @return this {@link Decisions} object
	 */
	Decisions retryActivity(Long scheduledEventId, ActivityTaskFailedContext context, RetryPolicy retryPolicy);

    /**
     * Automatically retries failed activity {@code activityType} with specified {@code retryPolicy}.
     *
     * @param scheduledEventId id of the ActivityTaskScheduled event that was recorded when activity task that failed was scheduled
     * @param activityType     the class of activity that needs to be retried
     * @param context          the {@link DecisionTaskContext}
     * @param retryPolicy      one of the {@link RetryPolicy} which should be used to retry the activity
     * @return this {@link Decisions} object
     */
    Decisions retryActivity(Long scheduledEventId, Class<?> activityType, DecisionTaskContext context, RetryPolicy retryPolicy);

    /**
     * Automatically retries failed activity {@code activityName} with specified {@code retryPolicy}.
     *
     * @param scheduledEventId id of the ActivityTaskScheduled event that was recorded when activity task that failed was scheduled
     * @param activityName     the name of the activity that needs to be retried
     * @param context          the {@link DecisionTaskContext}
     * @param retryPolicy      one of the {@link RetryPolicy} which should be used to retry the activity
     * @return this {@link Decisions} object
     */
    Decisions retryActivity(Long scheduledEventId, String activityName, DecisionTaskContext context, RetryPolicy retryPolicy);

    /**
     * Add a "schedule activity task" decision based on the previously scheduled activity.
     * The Id of the new activity will be replaced with the new one, all other parameters will remain.
     *
     * @param attributes the attributes of previously scheduled activity
     * @return this {@link Decisions} object
     */
    Decisions scheduleActivityTask(ActivityTaskScheduledEventAttributes attributes);

	/**
	 * Adds a "Cancel Workflow Execution" decision.
	 *
	 * @param details details of the cancellation, optional
	 * @return this {@link Decisions} object
	 */
	Decisions cancelWorkflow(String details);

	/**
	 * Adds a "Start Child Workflow Execution" decision.
	 *
	 * @param workflowType {@link WorkflowType} child workflow type, required
	 * @param workflowId   child workflow ID, required
	 * @return this {@link Decisions} object
	 */
	Decisions startChildWorkflow(Class<?> workflowType, String workflowId);

	/**
	 * Adds a "Start Child Workflow Execution" decision.
	 *
	 * @param workflowType {@link WorkflowType} child workflow type, required
	 * @param workflowId   child workflow ID, required
	 * @param input        the input for the workflow execution, optional
	 * @return this {@link Decisions} object
	 */
	Decisions startChildWorkflow(Class<?> workflowType, String workflowId, Object input);

	/**
	 * Adds a "Start Child Workflow Execution" decision.
	 *
	 * @param workflowType {@link WorkflowType} child workflow type, required
	 * @param workflowId   child workflow ID, required
	 * @param input        the input for the workflow execution, optional
	 * @param options      {@link WorkflowOptions} with additional parameters for child workflow execution, optional
	 * @return this {@link Decisions} object
	 */
	Decisions startChildWorkflow(Class<?> workflowType, String workflowId, Object input, WorkflowOptions options);

	/**
	 * Adds a "Request Cancel External Workflow Execution" decision.
	 *
	 * @param workflowId ID of workflow to cancel, required
	 * @param runId      run ID of workflow to cancel, required
	 * @return this {@link Decisions} object
	 */
	Decisions requestCancelExternalWorkflow(String workflowId, String runId);

	/**
	 * Adds a "Request Cancel External Workflow Execution" decision.
	 *
	 * @param workflowId ID of workflow to cancel, required
	 * @param runId      run ID of workflow to cancel, required
	 * @param control    data attached to the event that can be used by the decider in subsequent workflow tasks, optional
	 * @return this {@link Decisions} object
	 */
	Decisions requestCancelExternalWorkflow(String workflowId, String runId, Object control);
}
