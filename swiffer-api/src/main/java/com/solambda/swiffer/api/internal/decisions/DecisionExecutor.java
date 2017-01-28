package com.solambda.swiffer.api.internal.decisions;

import com.solambda.swiffer.api.Decisions;

/**
 * Instances of this interface apply the decisions made for a decision task, by
 * sending it to AWS SWF.
 */
public interface DecisionExecutor {

	/**
	 * Apply the decisions made in a particular context.
	 */
	public void apply(DecisionTaskContext context, Decisions decisions);
}
