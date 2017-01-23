package com.solambda.swiffer.api.internal.activities;

import com.solambda.swiffer.api.internal.HasInput;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.model.WorkflowHistory;

/**
 * Context of the task being executed.
 * <p>
 *
 */
public interface ActivityTaskContext extends HasInput {

	/**
	 * @return the history of the workflow
	 */
	WorkflowHistory history();

	/**
	 * @return the id of the task being executed, used to report the task
	 */
	String taskToken();

	/**
	 * @return the type of task being executed
	 */
	VersionedName activityType();

	/**
	 * @return the unique id of the task execution (generated)
	 */
	String activityId();

}
