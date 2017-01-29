package com.solambda.swiffer.api.internal.decisions;

import java.lang.reflect.InvocationTargetException;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.MethodInvoker;

public class EventHandlerImpl implements EventHandler {

	private EventHandlerType type;
	private MethodInvoker methodInvoker;
	private EventHandlerArgumentsProvider argumentsProvider;

	public EventHandlerImpl(
			final EventHandlerType type,
			final MethodInvoker methodInvoker,
			final EventHandlerArgumentsProvider argumentsProvider) {
		super();
		this.type = type;
		this.methodInvoker = methodInvoker;
		this.argumentsProvider = argumentsProvider;
	}

	@Override
	public String handleEvent(final EventContext context, final Decisions decisions)
			throws DecisionTaskExecutionException {
		try {
			final Object[] arguments = this.argumentsProvider.getArguments(context, decisions);
			final Object result = this.methodInvoker.invoke(arguments);
			if (result == null) {
				return null;
			}
			return result.toString();
		} catch (final InvocationTargetException e) {
			throw new DecisionTaskExecutionException(context, e.getTargetException());
		}
	}

	@Override
	public EventHandlerType getEventHandlerType() {
		return this.type;
	}

}
