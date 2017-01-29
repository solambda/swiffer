package com.solambda.swiffer.api.internal.decisions;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.solambda.swiffer.api.exceptions.WorkflowTemplateException;
import com.solambda.swiffer.api.internal.VersionedName;

public class EventHandlerRegistryFactory {

	private EventHandlerFactory factory;

	public EventHandlerRegistryFactory(final VersionedName workflowType) {
		this.factory = new EventHandlerFactory(workflowType);
	}

	public EventHandlerRegistry build(final Object template) {
		final Map<EventHandlerType, EventHandler> registry = new HashMap<>();
		fillRegistryByIntrospection(registry, template);
		return new EventHandlerRegistry(registry);
	}

	private void fillRegistryByIntrospection(final Map<EventHandlerType, EventHandler> registry,
			final Object template) {
		final Method[] methods = template.getClass().getMethods();
		for (final Method method : methods) {
			final EventHandler handler = this.factory.createEventHandler(template, method);
			if (handler != null) {
				final EventHandler existingHandler = registry.put(handler.getEventHandlerType(), handler);
				if (existingHandler != null) {
					throw new WorkflowTemplateException(String.format(
							"The template %s has more than one handler of %s", template,
							existingHandler.getEventHandlerType()));
				}
			}
		}
	}

}
