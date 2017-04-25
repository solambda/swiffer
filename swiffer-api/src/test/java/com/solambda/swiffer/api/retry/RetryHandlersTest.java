package com.solambda.swiffer.api.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import com.amazonaws.services.simpleworkflow.model.ActivityTaskScheduledEventAttributes;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.ActivityTaskFailedContext;
import com.solambda.swiffer.api.internal.context.ActivityTaskTimedOutContext;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskContext;
import com.solambda.swiffer.api.internal.decisions.WorkflowEvent;
import com.solambda.swiffer.api.internal.decisions.WorkflowHistory;


public class RetryHandlersTest {

    private static final Long SCHEDULED_EVENT_ID = 89L;
    private static final String ACTIVITY_NAME = "ActivityName";

    private final RetryPolicy retryPolicy = mock(RetryPolicy.class);
    private final RetryHandlers retryHandlers = new RetryHandlers(retryPolicy);
    private final Decisions decideTo = mock(Decisions.class);


    @Test
    public void customRetryPolicy() throws Exception {
        RetryPolicy retryPolicy = mock(RetryPolicy.class);

        RetryHandlers retryHandlers = new RetryHandlers(retryPolicy);

        assertThat(retryHandlers.getRetryPolicy()).isEqualTo(retryPolicy);
    }

    @Test
    public void onFailure() throws Exception {
        ActivityTaskFailedContext context = mockActivityTaskFailedContext(ACTIVITY_NAME);

        retryHandlers.onFailure(SCHEDULED_EVENT_ID, decideTo, context);

        verify(decideTo).retryActivity(SCHEDULED_EVENT_ID, ACTIVITY_NAME, context, retryPolicy);
    }

    @Test
    public void onTimeout() throws Exception {
        ActivityTaskTimedOutContext context = mockActivityTaskTimedOutContext(ACTIVITY_NAME);

        retryHandlers.onTimeout(SCHEDULED_EVENT_ID, decideTo, context);

        verify(decideTo).retryActivity(SCHEDULED_EVENT_ID, ACTIVITY_NAME, context, retryPolicy);
    }

    @Test
    public void onTimer() throws Exception {
        String markerName = RetryControl.RETRY_MARKER + ACTIVITY_NAME;
        when(decideTo.scheduleActivityTask(any())).thenReturn(decideTo);
        ActivityTaskScheduledEventAttributes activityAttributes = mock(ActivityTaskScheduledEventAttributes.class);
        WorkflowEvent event = mock(WorkflowEvent.class);
        when(event.getActivityTaskScheduledEventAttributes()).thenReturn(activityAttributes);
        WorkflowHistory history = mock(WorkflowHistory.class);
        when(history.getEventById(SCHEDULED_EVENT_ID)).thenReturn(event);
        DecisionTaskContext context = mock(DecisionTaskContext.class);
        when(context.getMarkerDetails(markerName, Integer.class)).thenReturn(Optional.of(4));
        when(context.history()).thenReturn(history);

        RetryControl control = new RetryControl(SCHEDULED_EVENT_ID, ACTIVITY_NAME);

        retryHandlers.onTimer(control, decideTo, context);

        verify(decideTo).scheduleActivityTask(activityAttributes);
        verify(decideTo).recordMarker(markerName, 5);
    }

    @Test
    public void onTimer_AfterCancelRequested() throws Exception {
        String markerName = RetryControl.RETRY_MARKER + ACTIVITY_NAME;

        ActivityTaskScheduledEventAttributes activityAttributes = mock(ActivityTaskScheduledEventAttributes.class);
        WorkflowEvent event = mock(WorkflowEvent.class);
        when(event.getActivityTaskScheduledEventAttributes()).thenReturn(activityAttributes);
        WorkflowHistory history = mock(WorkflowHistory.class);
        when(history.getEventById(SCHEDULED_EVENT_ID)).thenReturn(event);
        DecisionTaskContext context = mock(DecisionTaskContext.class);
        when(context.getMarkerDetails(markerName, Integer.class)).thenReturn(Optional.of(4));
        when(context.history()).thenReturn(history);
        when(context.isCancelRequested()).thenReturn(true);

        RetryControl control = mock(RetryControl.class);
        when(control.getScheduledEventId()).thenReturn(SCHEDULED_EVENT_ID);
        when(control.getMarkerName()).thenReturn(markerName);

        retryHandlers.onTimer(control, decideTo, context);

        verifyZeroInteractions(decideTo);
    }

    private ActivityTaskFailedContext mockActivityTaskFailedContext(String activityName) {
        VersionedName versionedName = mock(VersionedName.class);
        when(versionedName.name()).thenReturn(activityName);
        ActivityTaskFailedContext context = mock(ActivityTaskFailedContext.class);
        when(context.activityType()).thenReturn(versionedName);

        return context;
    }

    private ActivityTaskTimedOutContext mockActivityTaskTimedOutContext(String activityName) {
        VersionedName versionedName = mock(VersionedName.class);
        when(versionedName.name()).thenReturn(activityName);
        ActivityTaskTimedOutContext context = mock(ActivityTaskTimedOutContext.class);
        when(context.activityType()).thenReturn(versionedName);

        return context;
    }
}