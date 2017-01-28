package com.solambda.swiffer.api.internal.decisions;

import com.solambda.swiffer.api.Decisions;

public class WorkflowTemplateImpl implements WorkflowTemplate {

	private final EventHandlerRegistry eventHandlerRegistry;

	public WorkflowTemplateImpl(final EventHandlerRegistry eventHandlerRegistry) {
		super();
		this.eventHandlerRegistry = eventHandlerRegistry;
	}

	@Override
	public Decisions decide(final DecisionTaskContext decisionContext) {
		// final List<WorkflowEvent> newEvents = decisionContext.newEvents();
		// for (final WorkflowEvent event : newEvents) {
		// final EventHandlerType type = toType(event);
		// }
		// get an event handler
		// if none, determine what to do
		// if present, execute it
		//
		return null;
	}

	// private EventHandlerType toType(final WorkflowEvent event) {
	// return new EventHandlerType(event.);
	// }

}
