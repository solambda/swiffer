package com.solambda.swiffer.api.retry;

import java.time.Duration;
import java.util.Optional;

/**
 * Implementation of {@link RetryPolicy} which do not retry failed or timed out Activities.
 */
public class NoRetryPolicy implements RetryPolicy {

    /**
     * Creates new {@link NoRetryPolicy}.
     */
    public NoRetryPolicy() {
    }

    @Override
    public Optional<Duration> durationToNextTry(int nextAttemptNumber) {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "NoRetryPolicy";
    }
}
