package com.solambda.swiffer.api.internal.activities;

import com.solambda.swiffer.api.ActivityType;

public class NoActivityExecutorRegisteredException extends Exception {

	private ActivityType type;

	public NoActivityExecutorRegisteredException(final ActivityType type) {
		super();
		this.type = type;
	}

}
