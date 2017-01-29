package com.solambda.swiffer.api.exceptions;

import com.solambda.swiffer.api.ActivityType;

public class NoActivityExecutorRegisteredException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -41851126827387723L;
	private ActivityType type;

	public NoActivityExecutorRegisteredException(final ActivityType type) {
		super();
		this.type = type;
	}

}
