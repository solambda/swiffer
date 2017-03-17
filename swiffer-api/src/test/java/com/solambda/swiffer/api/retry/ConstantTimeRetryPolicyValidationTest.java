package com.solambda.swiffer.api.retry;

import java.time.Duration;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ConstantTimeRetryPolicyValidationTest {

    @Parameters
    public static Iterable<Object[]> params() {
        return Arrays.asList(new Object[][]{
                {null, -1},
                {Duration.ZERO, -1},
                {Duration.ofSeconds(-10), 10},
                {Duration.ofHours(1), 0}});
    }

    private final Duration waitTime;
    private final int maxAttempts;

    public ConstantTimeRetryPolicyValidationTest(Duration waitTime, int maxAttempts) {
        this.waitTime = waitTime;
        this.maxAttempts = maxAttempts;
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorTest() throws Exception {
        new ConstantTimeRetryPolicy(waitTime, maxAttempts);
    }
}