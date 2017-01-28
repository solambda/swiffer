package com.solambda.swiffer.api.internal.decisions;

import java.util.List;

import com.solambda.swiffer.api.internal.VersionedName;

/**
 * Provide information useful during decision making of workflow.
 * <p>
 *
 *
 */
public interface DecisionTaskContext {

	/**
	 * @return the history of the workflow
	 */
	WorkflowHistory history();

	/**
	 * @return the id of the context of decision, used to apply the decisions
	 */
	String taskToken();

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
