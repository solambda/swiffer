package com.solambda.swiffer.api.internal.decisions;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.RespondDecisionTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.DecisionsImpl;

public class DecisionExecutorImpl implements DecisionExecutor {

	private AmazonSimpleWorkflow swf;

	public DecisionExecutorImpl(final AmazonSimpleWorkflow swf) {
		super();
		this.swf = swf;
	}

	@Override
	public void apply(final DecisionTaskContext context, final Decisions decisions) {
		try {
			this.swf.respondDecisionTaskCompleted(new RespondDecisionTaskCompletedRequest()
					.withDecisions(((DecisionsImpl) decisions).get())
					// FIXME: why and how to get it ? (appart from externally ?)
					// .withExecutionContext(executionContext)
					.withTaskToken(context.taskToken()));
		} catch (final UnknownResourceException e) {
			throw new IllegalStateException(String.format("Cannot apply decisions for context %s", context), e);
		}
	}

}
