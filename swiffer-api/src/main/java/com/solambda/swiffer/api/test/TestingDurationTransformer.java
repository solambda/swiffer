package com.solambda.swiffer.api.test;

import java.time.Duration;

import com.solambda.swiffer.api.duration.DurationTransformer;

/**
 * This implementation of {@link DurationTransformer} should be used in tests.
 * Allows conversion of duration to another one to emulate passing time.
 */
public class TestingDurationTransformer implements DurationTransformer {
    /**
     * Hours per day.
     */
    static final int HOURS_PER_DAY = 24;
    /**
     * Minutes per hour.
     */
    static final int MINUTES_PER_HOUR = 60;

    /**
     * Seconds per minute.
     */
    static final int SECONDS_PER_MINUTE = 60;
    /**
     * Seconds per hour.
     */
    static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
    /**
     * Seconds per day.
     */
    static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

    private long factor;

    /**
     * Constructs new {@link DurationTransformer} without any transformations.
     */
    public TestingDurationTransformer() {
        factor = 1;
    }

    @Override
    public Duration transform(Duration originalDuration) {
        if (originalDuration == null) {
            return null;
        }

        return originalDuration.dividedBy(factor);
    }

    /**
     * Adjusts the target duration to a scale of provided hour duration, no less than a second.
     *
     * @param hourDuration new hour duration
     */
    public void setHourDuration(Duration hourDuration) {
        validateScaleDuration(hourDuration);
        factor = SECONDS_PER_HOUR / hourDuration.getSeconds();
    }

    /**
     * Adjusts the target duration to a scale of provided day duration, no less than a second..
     *
     * @param dayDuration new day duration
     */
    public void setDayDuration(Duration dayDuration) {
        validateScaleDuration(dayDuration);
        factor = SECONDS_PER_DAY / dayDuration.getSeconds();
    }

    /**
     * Adjusts the target duration to a scale of provided minute duration, no less than a second.
     *
     * @param minuteDuration new minute duration
     */
    public void setMinuteDuration(Duration minuteDuration) {
        validateScaleDuration(minuteDuration);
        factor = SECONDS_PER_MINUTE / minuteDuration.getSeconds();
    }

    private void validateScaleDuration(Duration duration){
        if (duration == null || duration.getSeconds() == 0) {
            throw new IllegalArgumentException("Expected duration scale should be at least 1 second.");
        }
    }
}
