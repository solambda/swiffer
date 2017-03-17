package com.solambda.swiffer.api.retry;

import java.time.Duration;
import java.util.Optional;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Implementation of {@link RetryPolicy} which retries Activity with constant time between attempts.
 */
public class ConstantTimeRetryPolicy implements RetryPolicy {

    private final Duration waitTime;
    private final long maxAttemptsNumber;

    /**
     * Creates new {@link ConstantTimeRetryPolicy} with specified time between retries
     * and infinite number of retry attempts.
     *
     * @param waitTime {@link Duration} representing wait time between retry attempts
     */
    public ConstantTimeRetryPolicy(Duration waitTime) {
        this(waitTime, -1);
    }

    /**
     * Creates new {@link ConstantTimeRetryPolicy} with specified time between retries and number of retry attempts.
     *
     * @param waitTime          {@link Duration} representing wait time between retry attempts
     * @param maxAttemptsNumber maximum number of retry attempts, greater than 0 or negative for infinite attempts
     */
    public ConstantTimeRetryPolicy(Duration waitTime, long maxAttemptsNumber) {
        validate(waitTime);
        Preconditions.checkArgument(maxAttemptsNumber != 0);

        this.waitTime = waitTime;
        this.maxAttemptsNumber = maxAttemptsNumber;
    }

    @Override
    public Optional<Duration> durationToNextTry(int nextAttemptNumber) {
        if (maxAttemptsNumber > 0 && nextAttemptNumber > maxAttemptsNumber) {
            return Optional.empty();
        }

        return Optional.of(waitTime);
    }

    private void validate(Duration duration) {
        Preconditions.checkArgument(duration != null);
        Preconditions.checkArgument(!duration.isNegative());
        Preconditions.checkArgument(!duration.isZero());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstantTimeRetryPolicy that = (ConstantTimeRetryPolicy) o;
        return maxAttemptsNumber == that.maxAttemptsNumber &&
                Objects.equal(waitTime, that.waitTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(waitTime, maxAttemptsNumber);
    }

    @Override
    public String toString() {
        return "ConstantTimeRetryPolicy{" +
                "waitTime=" + waitTime +
                ", maxAttemptsNumber=" + maxAttemptsNumber +
                '}';
    }
}
