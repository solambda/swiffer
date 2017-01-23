package com.solambda.swiffer.api.internal.activities;

public interface ActivityExecutor {

	String execute(ActivityTaskContext context) throws ActivityTaskExecutionFailedException;

}
