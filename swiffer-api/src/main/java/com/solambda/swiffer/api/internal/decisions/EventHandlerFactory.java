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
import com.solambda.swiffer.api.internal.handler.CloseWorkflowControl;
import com.solambda.swiffer.api.internal.handler.CloseWorkflowFailedHandlers;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.RetryControl;
import com.solambda.swiffer.api.retry.RetryHandlers;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class EventHandlerFactory {

	public static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerFactory.class);

	static final EventHandlerType FAILED_ACTIVITY = new EventHandlerType(EventType.ActivityTaskFailed, null);
	static final EventHandlerType TIMED_OUT_ACTIVITY = new EventHandlerType(EventType.ActivityTaskTimedOut, null);
	static final EventHandlerType RETRY_TIMER_FIRED = new EventHandlerType(EventType.TimerFired, null);

	static final EventHandlerType COMPLETE_WORKFLOW_EXECUTION_FAILED = new EventHandlerType(EventType.CompleteWorkflowExecutionFailed, null);
	static final EventHandlerType CANCEL_WORKFLOW_EXECUTION_FAILED = new EventHandlerType(EventType.CancelWorkflowExecutionFailed, null);
	static final EventHandlerType FAIL_WORKFLOW_EXECUTION_FAILED = new EventHandlerType(EventType.FailWorkflowExecutionFailed, null);
	static final EventHandlerType CONTINUE_AS_NEW_WORKFLOW_EXECUTION_FAILED = new EventHandlerType(EventType.ContinueAsNewWorkflowExecutionFailed, null);

	private EventHandlerTypeFactory eventHandlerTypeFactory;
	private EventHandlerArgumentsProviderFactory eventHandlerArgumentsProviderFactory;
	private final RetryHandlers retryHandlers;
	private final CloseWorkflowFailedHandlers closeWorkflowFailedHandlers;

	public EventHandlerFactory(final VersionedName workflowType, DataMapper dataMapper, RetryPolicy globalRetryPolicy) {
		super();
		this.eventHandlerTypeFactory = new EventHandlerTypeFactory(workflowType);
		this.eventHandlerArgumentsProviderFactory = new EventHandlerArgumentsProviderFactory(dataMapper);
		retryHandlers = new RetryHandlers(globalRetryPolicy);
		closeWorkflowFailedHandlers = new CloseWorkflowFailedHandlers();
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

	EventHandler createCompleteWorkflowExecutionFailedHandler() {
		try {
			Method method = closeWorkflowFailedHandlers.getClass().getMethod("onCompleteWorkflowExecutionFailed", CloseWorkflowControl.class, Decisions.class);
			return createEventHandler(closeWorkflowFailedHandlers, COMPLETE_WORKFLOW_EXECUTION_FAILED, method);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("Unable to create default CompleteWorkflowExecutionFailed handler", ex);
		}
	}

	EventHandler createCancelWorkflowExecutionFailedHandler() {
		try {
			Method method = closeWorkflowFailedHandlers.getClass().getMethod("onCancelWorkflowExecutionFailed", CloseWorkflowControl.class, Decisions.class);
			return createEventHandler(closeWorkflowFailedHandlers, CANCEL_WORKFLOW_EXECUTION_FAILED, method);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("Unable to create default CancelWorkflowExecutionFailed handler", ex);
		}
	}

	EventHandler createFailWorkflowExecutionFailedHandler() {
		try {
			Method method = closeWorkflowFailedHandlers.getClass().getMethod("onFailWorkflowExecutionFailed", CloseWorkflowControl.class, Decisions.class);
			return createEventHandler(closeWorkflowFailedHandlers, FAIL_WORKFLOW_EXECUTION_FAILED, method);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("Unable to create default FailWorkflowExecutionFailed handler", ex);
		}
	}

	EventHandler createContinueAsNewWorkflowExecutionFailedHandler() {
		try {
			Method method = closeWorkflowFailedHandlers.getClass().getMethod("onContinueAsNewWorkflowExecutionFailed", Decisions.class, DecisionTaskContext.class);
			return createEventHandler(closeWorkflowFailedHandlers, CONTINUE_AS_NEW_WORKFLOW_EXECUTION_FAILED, method);
		} catch (NoSuchMethodException ex) {
			throw new RuntimeException("Unable to create default ContinueAsNewWorkflowExecutionFailed handler", ex);
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
