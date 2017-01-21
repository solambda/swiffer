package com.solambda.aws.swiffer.api.model.tasks;

/**
 * Throw by a {@link TaskReport} when the task has been requested to
 * cancel.
 */
public class CancelRequested extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public CancelRequested() {
		super();
	}

}
