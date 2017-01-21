package com.solambda.aws.swiffer.api.model.decider;

import java.util.List;

import com.solambda.aws.swiffer.api.model.WorkflowHistory;
import com.solambda.aws.swiffer.api.model.WorkflowTypeId;

/**
 * Provide information useful during decision making of workflow.
 * <p>
 *
 *
 */
public interface DecisionContext {

	/**
	 * @return the history of the workflow
	 */
	WorkflowHistory history();

	/**
	 * @return the id of the context of decision, used to apply the decisions
	 */
	String decisionTaskId();

	/**
	 * @return the workflow type id, used to identify this workflow
	 */
	WorkflowTypeId workflowType();

	/**
	 * @return the new {@link WorkflowEvent}s received since the last
	 *         decision-making, sorted by ascending {@link WorkflowEvent#id()}s
	 *         (most recent last)
	 */
	List<WorkflowEvent> newEvents();

}
