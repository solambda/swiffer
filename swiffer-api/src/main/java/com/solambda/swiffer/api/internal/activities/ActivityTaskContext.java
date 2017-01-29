package com.solambda.swiffer.api.internal.activities;

import com.solambda.swiffer.api.internal.HasInput;
import com.solambda.swiffer.api.internal.TaskContext;
import com.solambda.swiffer.api.internal.VersionedName;

/**
 * Context of the task being executed.
 * <p>
 *
 */
public interface ActivityTaskContext extends TaskContext, HasInput {

	/**
	 * @return the type of task being executed
	 */
	VersionedName activityType();

	/**
	 * @return the unique id of the task execution
	 */
	String activityId();

}
