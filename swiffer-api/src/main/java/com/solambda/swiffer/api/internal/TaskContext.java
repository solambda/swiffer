package com.solambda.swiffer.api.internal;

import com.solambda.swiffer.api.internal.decisions.WorkflowHistory;

public interface TaskContext {

	/**
	 * @return the history of the workflow execution
	 */
	WorkflowHistory history();

	/**
	 * @return the id of the task being executed, used to respond to SWF
	 */
	public String taskToken();
}
