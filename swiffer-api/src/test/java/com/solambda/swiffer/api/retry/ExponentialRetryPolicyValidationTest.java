package com.solambda.swiffer.api.retry;

import java.time.Duration;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ExponentialRetryPolicyValidationTest {

    @Parameters
    public static Iterable<Object[]> params() {
        return Arrays.asList(new Object[][] {
                {null, null, -1},
                {null, Duration.ofSeconds(10), -1},
                {Duration.ofSeconds(10), null, -1},
                {Duration.ZERO, Duration.ZERO, -1},
                {Duration.ZERO, Duration.ofSeconds(10), 10},
                {Duration.ofSeconds(10), Duration.ZERO, 10},
                {Duration.ofSeconds(-10), Duration.ofSeconds(-10), 10},
                {Duration.ofSeconds(-10), Duration.ofSeconds(10), 10},
                {Duration.ofSeconds(10), Duration.ofSeconds(-10), 10},
                {Duration.ofHours(1), Duration.ofMinutes(59), 1},
                {Duration.ofSeconds(5), Duration.ofSeconds(6), 0},
        });
    }

    private final Duration baseDuration;
    private final Duration maxWaitTime;
    private final int maxAttempts;

    public ExponentialRetryPolicyValidationTest(Duration baseDuration, Duration maxWaitTime, int maxAttempts) {
        this.baseDuration = baseDuration;
        this.maxWaitTime = maxWaitTime;
        this.maxAttempts = maxAttempts;
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTest() throws Exception {
        new ExponentialRetryPolicy(baseDuration, maxWaitTime, maxAttempts);
    }

}