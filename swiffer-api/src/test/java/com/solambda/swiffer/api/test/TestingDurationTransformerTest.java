package com.solambda.swiffer.api.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.Test;

public class TestingDurationTransformerTest {

    private final TestingDurationTransformer testingDurationTransformer = new TestingDurationTransformer();

    @Test
    public void transform() {
        Duration originalDuration = Duration.ofDays(3).plusHours(10).plusMinutes(30);
        testingDurationTransformer.setHourDuration(Duration.ofSeconds(10));

        Duration result = testingDurationTransformer.transform(originalDuration);

        assertThat(result).isEqualTo(Duration.ofSeconds(825));
    }

    @Test
    public void transform_days() {
        Duration originalDuration = Duration.ofDays(5);
        testingDurationTransformer.setDayDuration(Duration.ofSeconds(10));

        Duration result = testingDurationTransformer.transform(originalDuration);

        assertThat(result).isEqualTo(Duration.ofSeconds(50));
    }

    @Test
    public void transform_hours() {
        Duration originalDuration = Duration.ofHours(23);
        testingDurationTransformer.setHourDuration(Duration.ofSeconds(40));

        Duration result = testingDurationTransformer.transform(originalDuration);

        assertThat(result).isEqualTo(Duration.ofSeconds(920));
    }

    @Test
    public void transform_minutes() {
        Duration originalDuration = Duration.ofMinutes(15);
        testingDurationTransformer.setMinuteDuration(Duration.ofSeconds(10));

        Duration result = testingDurationTransformer.transform(originalDuration);

        assertThat(result).isEqualTo(Duration.ofSeconds(150));
    }

    @Test
    public void transform_default() {
        Duration originalDuration = Duration.ofMinutes(15);

        Duration result = testingDurationTransformer.transform(originalDuration);

        assertThat(result).isEqualTo(originalDuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void transform_durationScaleIsLessThanSecond() {
        testingDurationTransformer.setMinuteDuration(Duration.ofNanos(10));

        testingDurationTransformer.transform(Duration.ofMinutes(15));
    }
}