package com.solambda.swiffer.api.retry;

import java.time.Duration;
import java.util.Optional;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Implementation of {@link RetryPolicy} which retries Activity with exponentially increasing time between attempts.
 */
public class ExponentialRetryPolicy implements RetryPolicy {

    private final Duration baseDuration;
    private final Duration maxWaitTime;
    private final long maxAttemptsNumber;

    /**
     * Creates new {@link ExponentialRetryPolicy} with specified base, maximum wait time
     * and infinite number of retry attempts.
     *
     * @param baseDuration duration to be used as multiplication factor for the exponent
     * @param maxWaitTime  maximum time between attempts (wait time will not not increase when this value is reached)
     */
    public ExponentialRetryPolicy(Duration baseDuration, Duration maxWaitTime) {
        this(baseDuration, maxWaitTime, -1);
    }

    /**
     * Creates new {@link ExponentialRetryPolicy} with specified base, maximum wait time and number of retry attempts.
     *
     * @param baseDuration      duration to be used as multiplication factor for the exponent
     * @param maxWaitTime       maximum time between attempts (wait time will not not increase when this value is reached)
     * @param maxAttemptsNumber maximum number of retry attempts, greater than 0 or negative for infinite attempts
     */
    public ExponentialRetryPolicy(Duration baseDuration, Duration maxWaitTime, long maxAttemptsNumber) {
        validate(baseDuration);
        validate(maxWaitTime);
        Preconditions.checkArgument(baseDuration.compareTo(maxWaitTime) < 0,
                                    "Maximum wait time time should be greater than the minimum wait time");
        Preconditions.checkArgument(maxAttemptsNumber != 0);

        this.baseDuration = baseDuration;
        this.maxWaitTime = maxWaitTime;
        this.maxAttemptsNumber = maxAttemptsNumber;
    }

    @Override
    public Optional<Duration> durationToNextTry(int nextAttemptNumber) {
        if (maxAttemptsNumber > 0 && nextAttemptNumber > maxAttemptsNumber) {
            return Optional.empty();
        }

        long factor = (long) (Math.pow(2, nextAttemptNumber) - 1);
        Duration nextDuration;
        try {
            nextDuration = baseDuration.multipliedBy(factor);
            if (nextDuration.compareTo(maxWaitTime) > 0) {
                nextDuration = maxWaitTime;
            }
        } catch (ArithmeticException ex) {
            // calculated duration is too long
            nextDuration = maxWaitTime;
        }

        return Optional.of(nextDuration);
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
        ExponentialRetryPolicy that = (ExponentialRetryPolicy) o;
        return maxAttemptsNumber == that.maxAttemptsNumber &&
                Objects.equal(baseDuration, that.baseDuration) &&
                Objects.equal(maxWaitTime, that.maxWaitTime);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(baseDuration, maxWaitTime, maxAttemptsNumber);
    }

    @Override
    public String toString() {
        return "ExponentialRetryPolicy{" +
                "baseDuration=" + baseDuration +
                ", maxWaitTime=" + maxWaitTime +
                ", maxAttemptsNumber=" + maxAttemptsNumber +
                '}';
    }
}
