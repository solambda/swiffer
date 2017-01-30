package com.solambda.swiffer.examples.utils;

import java.time.Duration;

public class Tests {

	public static final void sleep(final Duration d) {
		try {
			Thread.sleep(d.toMillis());
		} catch (final InterruptedException e) {
		}
	}
}
