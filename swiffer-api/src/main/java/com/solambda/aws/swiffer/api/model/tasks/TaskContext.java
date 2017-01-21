package com.solambda.aws.swiffer.api.model.tasks;

import com.solambda.aws.swiffer.api.model.HasInput;
import com.solambda.aws.swiffer.api.model.TaskType;
import com.solambda.aws.swiffer.api.model.WorkflowHistory;

/**
 * Context of the task being executed.
 * <p>
 *
 */
public interface TaskContext extends HasInput {

	/**
	 * @return the history of the workflow
	 */
	WorkflowHistory history();

	/**
	 * @return the id of the task being executed, used to report the task
	 */
	String contextId();

	/**
	 * @return the type of task being executed
	 */
	TaskType taskType();

	/**
	 * @return the unique id of the task execution (generated)
	 */
	String taskId();

}
