package com.solambda.swiffer.api.duration;

import java.time.Duration;

/**
 * Default implementation of {@link DurationTransformer}.
 * Doesn't perform any transformations.
 */
public class DefaultDurationTransformer implements DurationTransformer {

    @Override
    public Duration transform(Duration originalDuration) {
        return originalDuration;
    }
}
