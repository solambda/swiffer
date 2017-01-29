package com.solambda.swiffer.api.internal.decisions;

import com.solambda.swiffer.api.Decisions;

/**
 * Handle some event context and make decisions of it.
 * <p>
 */
public interface EventHandler {

	/**
	 * @param event
	 * @param decisions
	 * @return a string to be automatically registered as a marker, or none.
	 * @throws DecisionTaskExecutionException
	 */
	public String handleEvent(EventContext event, Decisions decisions) throws DecisionTaskExecutionException;

	/**
	 * @return the type of event handled by this handler
	 */
	public EventHandlerType getEventHandlerType();
}
