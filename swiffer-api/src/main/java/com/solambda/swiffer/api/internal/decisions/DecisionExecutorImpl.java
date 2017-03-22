package com.solambda.swiffer.api.internal.decisions;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.RespondDecisionTaskCompletedRequest;
import com.solambda.swiffer.api.Decisions;

public class DecisionExecutorImpl implements DecisionExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DecisionExecutorImpl.class);

	private AmazonSimpleWorkflow swf;

	public DecisionExecutorImpl(final AmazonSimpleWorkflow swf) {
		super();
		this.swf = swf;
	}

	@Override
	public void apply(final DecisionTaskContext context, final Decisions decisions) {
		final Collection<Decision> decisionList = ((DecisionsImpl) decisions).get();
		LOGGER.debug("Responding SWF with {} decisions: {}", decisionList.size(), decisions);
		this.swf.respondDecisionTaskCompleted(new RespondDecisionTaskCompletedRequest()
													  .withDecisions(decisionList)
													  // FIXME: why and how to get it ? (appart from externally ?)
													  // .withExecutionContext(executionContext)
													  .withTaskToken(context.taskToken()));
	}

}
