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
public class ConstantTimeRetryPolicyTest {

    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{{-1, Duration.ofSeconds(15), 1, Duration.ofSeconds(15)},
                                            {-1, Duration.ofSeconds(100), 20, Duration.ofSeconds(100)},
                                            {3, Duration.ofSeconds(35), 2, Duration.ofSeconds(35)},
                                            {4, Duration.ofSeconds(75), 4, Duration.ofSeconds(75)},
                                            {50, Duration.ofSeconds(155), 100, null}});
    }

    private final int attempt;
    private final Optional<Duration> expectedDuration;
    private final ConstantTimeRetryPolicy retryPolicy;

    public ConstantTimeRetryPolicyTest(int maxAttempts, Duration waitTime, int attempt, Duration expectedDuration) {
        this.attempt = attempt;
        this.expectedDuration = Optional.ofNullable(expectedDuration);
        retryPolicy = new ConstantTimeRetryPolicy(waitTime, maxAttempts);
    }

    @Test
    public void durationToNextTry() throws Exception {
        Optional<Duration> actualDuration = retryPolicy.durationToNextTry(attempt);

        assertThat(actualDuration).isEqualTo(expectedDuration);
    }

}