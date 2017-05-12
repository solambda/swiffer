package com.solambda.swiffer.api.internal.decisions;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.solambda.swiffer.api.exceptions.WorkflowTemplateException;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class EventHandlerRegistryFactory {

	private final EventHandlerFactory factory;

	public EventHandlerRegistryFactory(final VersionedName workflowType, DataMapper dataMapper, RetryPolicy retryPolicy) {
		this.factory = new EventHandlerFactory(workflowType, dataMapper, retryPolicy);
	}

	public EventHandlerRegistry build(final Object template) {
		final Map<EventHandlerType, EventHandler> registry = new HashMap<>();
		fillRegistryByIntrospection(registry, template);

        EventHandlerRegistry eventHandlerRegistry = new EventHandlerRegistry(registry);
        eventHandlerRegistry.setDefaultFailedActivityHandler(factory.createFailedActivityHandler());
        eventHandlerRegistry.setDefaultTimedOutActivityHandler(factory.createTimedOutActivityHandler());
        eventHandlerRegistry.setDefaultRetryTimerFiredHandler(factory.createRetryTimerFiredHandler());

		eventHandlerRegistry.setDefaultCancelWorkflowExecutionFailedHandler(factory.createCancelWorkflowExecutionFailedHandler());
		eventHandlerRegistry.setDefaultCompleteWorkflowExecutionFailedHandler(factory.createCompleteWorkflowExecutionFailedHandler());
		eventHandlerRegistry.setDefaultContinueAsNewWorkflowExecutionFailedHandler(factory.createContinueAsNewWorkflowExecutionFailedHandler());
		eventHandlerRegistry.setDefaultFailWorkflowExecutionFailedHandler(factory.createFailWorkflowExecutionFailedHandler());

        return eventHandlerRegistry;
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
