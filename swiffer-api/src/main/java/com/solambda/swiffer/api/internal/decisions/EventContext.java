package com.solambda.swiffer.api.internal.decisions;

import com.solambda.swiffer.api.internal.context.identifier.ContextName;
import com.solambda.swiffer.api.internal.events.EventCategory;

public interface EventContext extends DecisionTaskContext {

	// should be on the event context level
	/**
	 * @return the category the context belongs to
	 */
	EventCategory category();

	/**
	 * @return the name of the context (signal name, task type, workflow type,
	 *         timer name, etc.)
	 */
	ContextName name();

	/**
	 * @return the current event
	 */
	WorkflowEvent event();

}
