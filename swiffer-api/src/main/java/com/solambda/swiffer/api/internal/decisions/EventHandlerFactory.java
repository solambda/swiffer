package com.solambda.swiffer.api.internal.decisions;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.swiffer.api.internal.MethodInvoker;
import com.solambda.swiffer.api.internal.VersionedName;

public class EventHandlerFactory {

	public static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerFactory.class);

	private EventHandlerTypeFactory eventHandlerTypeFactory;
	private EventHandlerArgumentsProviderFactory eventHandlerArgumentsProviderFactory;

	public EventHandlerFactory(final VersionedName workflowType) {
		super();
		this.eventHandlerTypeFactory = new EventHandlerTypeFactory(workflowType);
		this.eventHandlerArgumentsProviderFactory = new EventHandlerArgumentsProviderFactory();
	}

	public EventHandler createEventHandler(final Object template, final Method method) {
		final EventHandlerType type = this.eventHandlerTypeFactory.create(method);
		if (type == null) {
			return null;
		} else {
			LOGGER.debug("Found event handler for {}", type);
			final EventHandler handler = createEventHandler(template, type, method);
			return handler;
		}
	}

	private EventHandler createEventHandler(final Object template,
			final EventHandlerType handlerType,
			final Method method) {
		final MethodInvoker methodInvoker = new MethodInvoker(template, method);
		// verify the arguments are ok,
		final EventHandlerArgumentsProvider argumentsProvider = this.eventHandlerArgumentsProviderFactory
				.createArgumentsProvider(handlerType.getEventType(), method);
		// and the return value also
		checkReturnType(method);
		return new EventHandlerImpl(handlerType, methodInvoker, argumentsProvider);
	}

	private void checkReturnType(final Method method) {
		final Class<?> returnType = method.getReturnType();
		// return type can be either:
		// - void (if there is one parameter of type Decisions)
		// - Decisions (if there is no parameter of type Decisions)
		// - Collection<Decision> (if there is no parameter of type Decisions)
		// - any other type is considered as a state change that should be
		// recorded as a marker ?
	}

}
