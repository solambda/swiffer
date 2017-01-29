package com.solambda.swiffer.api.internal.decisions;

public class DecisionTaskExecutionException extends Exception {

	private EventContext context;

	public DecisionTaskExecutionException(final EventContext context, final Throwable cause) {
		super(cause);
		this.context = context;
	}

	public EventContext getContext() {
		return this.context;
	}

}
