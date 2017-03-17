package com.solambda.swiffer.api.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NoRetryPolicyTest {

    @Parameters
    public static Collection<Object[]> params() {
        return IntStream.range(1, 101).boxed().map(integer -> new Object[]{integer}).collect(Collectors.toList());
    }

    private final RetryPolicy retryPolicy = new NoRetryPolicy();
    private final int attempt;

    public NoRetryPolicyTest(int attempt) {
        this.attempt = attempt;
    }

    @Test
    public void durationToNextTry() throws Exception {
        Optional<Duration> duration = retryPolicy.durationToNextTry(attempt);

        assertThat(duration.isPresent()).isFalse();
    }

}