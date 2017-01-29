package com.solambda.swiffer.api.internal.decisions;

import com.solambda.swiffer.api.Decisions;

public interface EventHandlerArgumentsProvider {

	/**
	 * Transform the given context into an array of arguments.
	 * 
	 * @param context
	 * @param decisions
	 * @return
	 */
	public Object[] getArguments(EventContext context, Decisions decisions);

}
