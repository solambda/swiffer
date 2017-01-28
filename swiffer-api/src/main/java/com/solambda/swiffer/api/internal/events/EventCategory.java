package com.solambda.swiffer.api.internal.events;

/**
 * Category of event
 */
public enum EventCategory {
	LAMBDA,
	ACTIVITY,
	TIMER,
	CHILD_WORKFLOW,
	MARKER,
	DECISION,
	WORKFLOW_EXECUTION,
	CANCEL_EXTERNAL_WORKFLOW,
	SIGNAL_EXTERNAL_WORKFLOW,
	SIGNAL;
	// CONTINUE_AS_NEW,
}
