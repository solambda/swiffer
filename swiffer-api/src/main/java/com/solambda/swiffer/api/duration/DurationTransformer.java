package com.solambda.swiffer.api.duration;

import java.time.Duration;

/**
 * Interface to transform provided {@link Duration} to another.
 * I.e duration of 1 day could be transformed to 1 hour.
 */
public interface DurationTransformer {

    /**
     * Transforms {@code duration} to another.
     *
     * @param originalDuration original duration
     * @return new {@link Duration} corresponding to the {@code originalDuration}
     */
    Duration transform(Duration originalDuration);
}
