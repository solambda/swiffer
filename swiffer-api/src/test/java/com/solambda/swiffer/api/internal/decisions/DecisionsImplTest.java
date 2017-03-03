package com.solambda.swiffer.api.internal.decisions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.Test;

import com.solambda.swiffer.api.ActivityOptions;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.mapper.DataMapper;

/**
 * Test for {@link DecisionsImplTest}.
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

    @ActivityType(name = "activity", version="1")
    @interface CustomActivity{}
}