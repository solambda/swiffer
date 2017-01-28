package com.solambda.swiffer.api.internal.activities.exceptions;

import com.solambda.swiffer.api.internal.activities.ActivityTaskContext;

public class ActivityTaskExecutionFailedException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
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
