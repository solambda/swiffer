package com.solambda.aws.swiffer.api.model.decider;

import java.util.Collection;

import com.amazonaws.services.simpleworkflow.model.Decision;

/**
 * Provide sensible defaults to non-handled events.
 * <p>
 */
public class DefaultEventDelegatorDecider implements Decider {

	private EventDelegatorDecider delegate;

	public DefaultEventDelegatorDecider(final EventDelegatorDecider delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public void makeDecisions(final DecisionContext context, final Decisions decideTo) {
		Collection<Decision> initialDecisions = decideTo.get();
		delegate.makeDecisions(context, decideTo);
		if (initialDecisions.size() == 0) {

		}
	}

}
