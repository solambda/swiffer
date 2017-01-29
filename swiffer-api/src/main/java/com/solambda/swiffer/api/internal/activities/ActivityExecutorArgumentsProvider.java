package com.solambda.swiffer.api.internal.activities;

public interface ActivityExecutorArgumentsProvider {

	/**
	 * Transform the given context into an array of arguments.
	 *
	 * @param context
	 * @return
	 */
	public Object[] getArguments(ActivityTaskContext context);
}
