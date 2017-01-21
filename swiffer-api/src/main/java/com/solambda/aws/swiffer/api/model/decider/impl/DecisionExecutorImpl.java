package com.solambda.aws.swiffer.api.model.decider.impl;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.RespondDecisionTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.solambda.aws.swiffer.api.model.decider.DecisionContext;
import com.solambda.aws.swiffer.api.model.decider.DecisionExecutor;
import com.solambda.aws.swiffer.api.model.decider.Decisions;

public class DecisionExecutorImpl implements DecisionExecutor {

	private AmazonSimpleWorkflow swf;

	public DecisionExecutorImpl(final AmazonSimpleWorkflow swf) {
		super();
		this.swf = swf;
	}

	@Override
	public void applyDecisions(final DecisionContext context, final Decisions decisions) {
		try {
			swf.respondDecisionTaskCompleted(new RespondDecisionTaskCompletedRequest()
					.withDecisions(decisions.get())
					// .withExecutionContext(executionContext)
					.withTaskToken(context.decisionTaskId())
					);
		} catch (UnknownResourceException e) {
			throw new IllegalStateException(String.format("Cannot apply decisions for context %s", context), e);
		}
	}

}
