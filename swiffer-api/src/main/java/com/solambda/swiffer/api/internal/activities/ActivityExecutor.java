package com.solambda.swiffer.api.internal.activities;

import com.solambda.swiffer.api.exceptions.ActivityTaskExecutionFailedException;

public interface ActivityExecutor {

	String execute(ActivityTaskContext context) throws ActivityTaskExecutionFailedException;

}
