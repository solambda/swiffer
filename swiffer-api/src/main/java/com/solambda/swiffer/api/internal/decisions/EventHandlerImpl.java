package com.solambda.swiffer.api.internal.decisions;

import java.lang.reflect.InvocationTargetException;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.ArgumentsProvider;
import com.solambda.swiffer.api.internal.MethodInvoker;

public class EventHandlerImpl implements EventHandler {

	private MethodInvoker methodInvoker;
	private ArgumentsProvider<EventContext> argumentsProvider;

	public EventHandlerImpl(final MethodInvoker methodInvoker,
			final ArgumentsProvider<EventContext> argumentsProvider) {
		super();
		this.methodInvoker = methodInvoker;
		this.argumentsProvider = argumentsProvider;
	}

	@Override
	public String handleEvent(final EventContext context, final Decisions decisions)
			throws DecisionTaskExecutionFailedException {
		try {
			final Object[] arguments = this.argumentsProvider.getArguments(context);
			final Object result = this.methodInvoker.invoke(arguments);
			if (result == null) {
				return null;
			}
			return result.toString();
		} catch (final InvocationTargetException e) {
			throw new DecisionTaskExecutionFailedException(context, e.getTargetException());
		}
	}

}
