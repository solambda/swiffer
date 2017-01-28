package com.solambda.swiffer.api.internal.decisions;

import java.util.Map;

public class EventHandlerRegistry {

	private final Map<EventHandlerType, EventHandler> eventHandlerRegistry;

	public EventHandlerRegistry(final Map<EventHandlerType, EventHandler> eventHandlerRegistry) {
		super();
		this.eventHandlerRegistry = eventHandlerRegistry;
	}

	public EventHandler get(final EventHandlerType key) {
		return this.eventHandlerRegistry.get(key);
	}
}
