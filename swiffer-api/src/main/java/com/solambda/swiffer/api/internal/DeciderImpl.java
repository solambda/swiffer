package com.solambda.swiffer.api.internal;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.Decider;
import com.solambda.swiffer.api.model.decider.ContextProvider;
import com.solambda.swiffer.api.model.decider.DecisionContext;

public class DeciderImpl extends AbstractTaskListPoller<DecisionContext> implements Decider {

	public DeciderImpl(final AmazonSimpleWorkflow swf, final String domain, final String taskList,
			final String identity,
			final ContextProvider<DecisionContext> provider) {
		super(swf, domain, taskList, identity, provider);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void executeTask(final DecisionContext task) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeTaskImmediately(final DecisionContext task) {
		// TODO Auto-generated method stub

	}

}
