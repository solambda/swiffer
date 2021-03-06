package com.solambda.swiffer.api.exceptions;

import com.solambda.swiffer.api.internal.activities.ActivityExecutionReporter;

/**
 * Throw by a {@link ActivityExecutionReporter} when the task has been requested to
 * cancel.
 */
public class CancelActivityRequested extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public CancelActivityRequested() {
		super();
	}

}
