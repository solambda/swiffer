package com.solambda.swiffer.api.internal.decisions;

public class DecisionTaskExecutionFailedException extends Exception {

	private EventContext context;

	public DecisionTaskExecutionFailedException(final EventContext context, final Throwable cause) {
		super(cause);
		this.context = context;
	}

	public EventContext getContext() {
		return this.context;
	}

}
