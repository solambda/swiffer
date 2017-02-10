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
     * Minutes per day.
     */
    static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY;
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

    private Duration minuteDuration;
    private Duration hourDuration;
    private Duration dayDuration;

    /**
     * Constructs new {@link DurationTransformer} with expected durations for an minute, hour and day set to 1 second
     */
    public TestingDurationTransformer() {
        minuteDuration = Duration.ofSeconds(1);
        hourDuration = Duration.ofSeconds(1);
        dayDuration = Duration.ofSeconds(1);
    }

    @Override
    public Duration transform(Duration originalDuration) {
        if (originalDuration == null) {
            return null;
        }
        long seconds = originalDuration.getSeconds();
        long days = originalDuration.toDays();

        seconds = seconds - days * SECONDS_PER_DAY;
        long hours = seconds / SECONDS_PER_HOUR;

        seconds = seconds - hours * SECONDS_PER_HOUR;
        long minutes = seconds / SECONDS_PER_MINUTE;

        seconds = seconds - minutes * SECONDS_PER_MINUTE;

        return dayDuration.multipliedBy(days)
                          .plus(hourDuration.multipliedBy(hours))
                          .plus(minuteDuration.multipliedBy(minutes))
                          .plusSeconds(seconds);
    }

    /**
     * Sets expected duration of an hour.
     *
     * @param hourDuration new hour duration
     */
    public void setHourDuration(Duration hourDuration) {
        this.hourDuration = hourDuration;
    }

    /**
     * Sets expected duration for a day.
     *
     * @param dayDuration new day duration
     */
    public void setDayDuration(Duration dayDuration) {
        this.dayDuration = dayDuration;
    }

    /**
     * Sets expected duration for a minute.
     *
     * @param minuteDuration new minute duration
     */
    public void setMinuteDuration(Duration minuteDuration) {
        this.minuteDuration = minuteDuration;
    }
}
