package com.solambda.swiffer.api.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ExponentialRetryPolicyTest {

    private static final Duration MAX_WAIT_TIME = Duration.ofHours(1);
    private static final Duration MIN_WAIT_TIME = Duration.ofSeconds(5);

    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{{1, MIN_WAIT_TIME, -1},
                                            {2, Duration.ofSeconds(15), -1},
                                            {3, Duration.ofSeconds(35), -1},
                                            {4, Duration.ofSeconds(75), -1},
                                            {5, Duration.ofSeconds(155), -1},
                                            {6, Duration.ofSeconds(315), -1},
                                            {7, Duration.ofSeconds(635), -1},
                                            {8, Duration.ofSeconds(1275), -1},
                                            {9, Duration.ofSeconds(2555), -1},
                                            {10, MAX_WAIT_TIME, -1},
                                            {11, MAX_WAIT_TIME, -1},
                                            {100, MAX_WAIT_TIME, -1},
                                            {100, null, 1},
                                            {9, Duration.ofSeconds(2555), 9},
                                            {10, MAX_WAIT_TIME, 10},
        });
    }

    private final int attempt;
    private final Optional<Duration> expectedDuration;
    private final ExponentialRetryPolicy retryPolicy;

    public ExponentialRetryPolicyTest(int attempt, Duration expectedDuration, int maxAttempts) {
        this.attempt = attempt;
        this.expectedDuration = Optional.ofNullable(expectedDuration);
        retryPolicy = new ExponentialRetryPolicy(MIN_WAIT_TIME, MAX_WAIT_TIME, maxAttempts);
    }

    @Test
    public void durationToNextRetry() {
        Optional<Duration> actualDuration = retryPolicy.durationToNextTry(attempt);

        assertThat(actualDuration).isEqualTo(expectedDuration);
    }
}