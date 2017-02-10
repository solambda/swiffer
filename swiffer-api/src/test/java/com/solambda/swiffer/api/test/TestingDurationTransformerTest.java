package com.solambda.swiffer.api.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.Test;

public class TestingDurationTransformerTest {

    private final TestingDurationTransformer testingDurationTransformer = new TestingDurationTransformer();

    @Test
    public void transform() {
        Duration originalDuration = Duration.ofDays(3).plusHours(17).plusMinutes(25);

        Duration result = testingDurationTransformer.transform(originalDuration);

        assertThat(result).isEqualTo(Duration.ofSeconds(45));
    }

    @Test
    public void transform_days() {
        Duration originalDuration = Duration.ofDays(5);
        testingDurationTransformer.setDayDuration(Duration.ofNanos(10));

        Duration result = testingDurationTransformer.transform(originalDuration);

        assertThat(result).isEqualTo(Duration.ofNanos(50));
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
    public void transform_OnlyDays() {
        Duration originalDuration = Duration.ofDays(3).plusHours(17).plusMinutes(25);
        testingDurationTransformer.setDayDuration(Duration.ofNanos(10));
        testingDurationTransformer.setHourDuration(Duration.ZERO);
        testingDurationTransformer.setMinuteDuration(Duration.ZERO);

        Duration result = testingDurationTransformer.transform(originalDuration);

        assertThat(result).isEqualTo(Duration.ofNanos(30));
    }
}