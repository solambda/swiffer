package com.solambda.swiffer.test;

import java.time.Duration;

import org.mockito.stubbing.Answer;

public class Tests {

	public static final String DOMAIN = "github-swiffer-domain";

	public static <T> Answer<T> returnAfterDelay(final T response, final Duration delay) {

		return (i) -> {
			sleep(delay);
			return response;
		};
	}

	public static void sleep(final Duration delay) {
		try {
			Thread.sleep(delay.toMillis());
		} catch (final InterruptedException e) {
		}
	}
}
