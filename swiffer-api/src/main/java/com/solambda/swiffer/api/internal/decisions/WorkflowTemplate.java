package com.solambda.swiffer.api.internal.decisions;

import com.solambda.swiffer.api.Decisions;

/**
 * A workflow template is the runtime representation of a user-defined workflow
 * instance, invoking the user-defined event handlers and returning a
 * {@link Decisions} object.
 * <p>
 * Instances of {@link WorkflowTemplate} are created by a
 * {@link WorkflowTemplateFactory}.
 * <p>
 *
 */
public interface WorkflowTemplate {

	public Decisions decide(DecisionTaskContext decisionContext);
}
