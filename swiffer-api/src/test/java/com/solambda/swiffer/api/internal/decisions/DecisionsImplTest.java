package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.solambda.swiffer.api.ActivityOptions;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.RetryControl;
import com.solambda.swiffer.api.retry.RetryPolicy;

/**
 * Test for {@link DecisionsImpl}.
 */
public class DecisionsImplTest {
    private final DataMapper dataMapper = mock(DataMapper.class);
    private final DurationTransformer durationTransformer = mock(DurationTransformer.class);
    private final Decisions decisions = new DecisionsImpl(dataMapper, durationTransformer);

    /**
     * Verify that marker details are serialized.
     */
    @Test
    public void recordMarker() {
        Object markerDetails = new Object();

        decisions.recordMarker("Marker", markerDetails);

        verify(dataMapper).serialize(markerDetails);
    }

    /**
     * Verify activity input is serialized.
     */
    @Test
    public void scheduleActivityTask() {
        Object input = new Object();

        decisions.scheduleActivityTask(CustomActivity.class, input);

        verify(dataMapper).serialize(input);
    }

    /**
     * Verify activity input is serialized.
     */
    @Test
    public void scheduleActivityTask_withOptions() {
        Object input = new Object();

        decisions.scheduleActivityTask(CustomActivity.class, input, mock(ActivityOptions.class));

        verify(dataMapper).serialize(input);
    }

    /**
     * Verify activity input is serialized.
     */
    @Test
    public void scheduleActivityTask_withActivityId() {
        Object input = new Object();

        decisions.scheduleActivityTask(CustomActivity.class, input, "activityId", mock(ActivityOptions.class));

        verify(dataMapper).serialize(input);
    }

    /**
     * Verify complete workflow result is serialized.
     */
    @Test
    public void completeWorkflow() {
        Object result = new Object();

        decisions.completeWorkflow(result);

        verify(dataMapper).serialize(result);
    }

    /**
     * Verify timer control is serialized; duration is passed through {@link DurationTransformer}
     */
    @Test
    public void startTimer() {
        Object control = new Object();
        Duration duration = Duration.ofDays(5);

        when(durationTransformer.transform(duration)).thenReturn(duration);

        decisions.startTimer("timerId", duration, control);

        verify(dataMapper).serialize(control);
        verify(durationTransformer).transform(duration);
    }

    @Test
    public void retryActivity() {
        Duration nextDuration = Duration.ofSeconds(10);
        String expectedTimerId = RetryControl.RETRY_TIMER + "activity";
        Decisions spy = spy(decisions);
        Long scheduledEventId = 777L;
        DecisionTaskContext context = mock(DecisionTaskContext.class);
        when(context.getMarkerDetails(anyString(), any())).thenReturn(Optional.of(5));
        when(durationTransformer.transform(nextDuration)).thenReturn(nextDuration);

        RetryPolicy retryPolicy = mock(RetryPolicy.class);
        when(retryPolicy.durationToNextTry(6)).thenReturn(Optional.of(nextDuration));

        spy.retryActivity(scheduledEventId, CustomActivity.class, context, retryPolicy);

        ArgumentCaptor<RetryControl> controlArgumentCaptor = ArgumentCaptor.forClass(RetryControl.class);
        verify(spy).startTimer(eq(expectedTimerId), eq(nextDuration), controlArgumentCaptor.capture());
        RetryControl retryTimerControl = controlArgumentCaptor.getValue();
        assertThat(retryTimerControl.getScheduledEventId()).isEqualTo(scheduledEventId);
    }

    @Test
    public void retryActivity_NoRetry() {
        Decisions spy = spy(decisions);
        Long scheduledEventId = 777L;
        DecisionTaskContext context = mock(DecisionTaskContext.class);
        when(context.getMarkerDetails(anyString(), any())).thenReturn(Optional.empty());

        RetryPolicy retryPolicy = mock(RetryPolicy.class);
        when(retryPolicy.durationToNextTry(1)).thenReturn(Optional.empty());

        spy.retryActivity(scheduledEventId, "act", context, retryPolicy);

        verify(spy, never()).startTimer(any(), any(), any());
    }


    @ActivityType(name = "activity", version="1")
    @interface CustomActivity{}
}