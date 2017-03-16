package com.solambda.swiffer.api.internal.decisions;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.MethodInvoker;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.ActivityTaskFailedContext;
import com.solambda.swiffer.api.internal.context.ActivityTaskTimedOutContext;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.RetryControl;
import com.solambda.swiffer.api.retry.RetryHandlers;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class EventHandlerFactory {

	public static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerFactory.class);

	static final EventHandlerType FAILED_ACTIVITY = new EventHandlerType(EventType.ActivityTaskFailed, null);
	static final EventHandlerType TIMED_OUT_ACTIVITY = new EventHandlerType(EventType.ActivityTaskTimedOut, null);
	static final EventHandlerType RETRY_TIMER_FIRED = new EventHandlerType(EventType.TimerFired, null);

	private EventHandlerTypeFactory eventHandlerTypeFactory;
	private EventHandlerArgumentsProviderFactory eventHandlerArgumentsProviderFactory;
	private final RetryHandlers retryHandlers;

	public EventHandlerFactory(final VersionedName workflowType, DataMapper dataMapper, RetryPolicy globalRetryPolicy) {
		super();
		this.eventHandlerTypeFactory = new EventHandlerTypeFactory(workflowType);
		this.eventHandlerArgumentsProviderFactory = new EventHandlerArgumentsProviderFactory(dataMapper);
		retryHandlers = new RetryHandlers(globalRetryPolicy);
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

	EventHandler createEventHandler(final Object template,
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

	EventHandler createFailedActivityHandler() {
		try {
			Method method = retryHandlers.getClass().getMethod("onFailure", Long.class, Decisions.class, ActivityTaskFailedContext.class);
			return retryHandlers.shouldRetry() ? createEventHandler(retryHandlers, FAILED_ACTIVITY, method) : null;
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("Unable to create default handler for failed activity", ex);
		}
	}

	EventHandler createTimedOutActivityHandler() {
		try {
			Method method = retryHandlers.getClass().getMethod("onTimeout", Long.class, Decisions.class, ActivityTaskTimedOutContext.class);
			return retryHandlers.shouldRetry() ? createEventHandler(retryHandlers, TIMED_OUT_ACTIVITY, method) : null;
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("Unable to create default handler for timed out activity", ex);
		}
	}

	EventHandler createRetryTimerFiredHandler() {
		try {
			Method method = retryHandlers.getClass().getMethod("onTimer", RetryControl.class, Decisions.class, DecisionTaskContext.class);
			return createEventHandler(retryHandlers, RETRY_TIMER_FIRED, method);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("Unable to create default retry timer handler", ex);
		}
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
