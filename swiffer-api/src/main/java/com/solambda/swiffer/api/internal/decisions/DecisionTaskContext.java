package com.solambda.swiffer.api.internal.decisions;

import java.util.List;

import com.solambda.swiffer.api.internal.TaskContext;
import com.solambda.swiffer.api.internal.VersionedName;

/**
 * Provide information useful during decision making of a workflow.
 * <p>
 *
 *
 */
public interface DecisionTaskContext extends TaskContext {

	/**
	 * @return the workflow type id, used to identify this workflow
	 */
	VersionedName workflowType();

	/**
	 * @return the new {@link WorkflowEvent}s received since the last
	 *         decision-making, sorted by ascending {@link WorkflowEvent#id()}s
	 *         (most recent last)
	 */
	List<WorkflowEvent> newEvents();

}
