package com.solambda.swiffer.api.internal.decisions;

import com.solambda.swiffer.api.Decisions;

public interface EventHandler {

	public String handleEvent(EventContext event, Decisions decisions) throws DecisionTaskExecutionFailedException;
}
