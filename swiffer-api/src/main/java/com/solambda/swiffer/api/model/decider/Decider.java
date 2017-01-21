package com.solambda.swiffer.api.model.decider;

public interface Decider {

	public void makeDecisions(DecisionContext context, Decisions decideTo);

}
