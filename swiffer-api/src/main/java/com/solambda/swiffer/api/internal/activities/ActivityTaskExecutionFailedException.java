package com.solambda.swiffer.api.internal.activities;

public class ActivityTaskExecutionFailedException extends Exception {

	private ActivityTaskContext context;

	public ActivityTaskExecutionFailedException(final ActivityTaskContext context, final Throwable cause) {
		super(cause);
		this.context = context;
	}

	@Override
	public String getMessage() {
		return String.format("Execution of activity %s, id=%s failed!",
				this.context.activityType(), this.context.activityId());
	}
}
