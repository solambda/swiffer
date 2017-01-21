package com.solambda.aws.swiffer.api.model.decider;

public interface DecisionExecutor {

	/**
	 * Apply decisions.
	 */
	public void applyDecisions(DecisionContext context, Decisions decisions);
}
