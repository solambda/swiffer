package com.solambda.aws.swiffer.api.model;

public enum WorkflowEventState {
	/**
	 * No events exist.
	 */
	NOT_STARTED,

	/**
	 * Initial event.
	 */
	INITIAL,

	/**
	 * Event representing an ongoing.
	 */
	ACTIVE,

	CANCELED,

	/**
	 * Event representing a task that has completed successfully.
	 */
	SUCCESS,

	/**
	 * Event representing a task that has failed.
	 */
	ERROR,

	TIMEOUT;
}
