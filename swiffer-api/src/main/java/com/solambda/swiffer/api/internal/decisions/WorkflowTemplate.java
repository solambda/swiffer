package com.solambda.swiffer.api.internal.decisions;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.VersionedName;

/**
 * A workflow template is the runtime object of a user-defined workflow
 * instance, turning a {@link DecisionTaskContext} into a {@link Decisions}
 * object.
 * <p>
 * Instances of {@link WorkflowTemplate} are created by a
 * {@link WorkflowTemplateFactory}.
 * <p>
 *
 */
public interface WorkflowTemplate {

	public Decisions decide(DecisionTaskContext decisionContext) throws DecisionTaskExecutionException;

	/**
	 * @return the type of workflow this template can handle.
	 */
	public VersionedName getWorkflowType();
}
