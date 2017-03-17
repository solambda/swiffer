package com.solambda.swiffer.api.retry;

import java.time.Duration;
import java.util.Optional;

/**
 * Retry policy for failed or timed out Activity.
 */
public interface RetryPolicy {

    /**
     * Returns duration till the next retry attempt.
     *
     * @param nextAttemptNumber next attempt number
     * @return {@link Optional} of Duration till the next attempt,
     * or {@link Optional#empty()} if next attempt shouldn't be made
     */
    Optional<Duration> durationToNextTry(int nextAttemptNumber);
}
