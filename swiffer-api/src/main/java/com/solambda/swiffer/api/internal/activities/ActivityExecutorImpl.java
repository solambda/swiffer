package com.solambda.swiffer.api.internal.activities;

import java.lang.reflect.InvocationTargetException;

import org.assertj.core.util.Preconditions;

import com.solambda.swiffer.api.internal.ArgumentsProvider;
import com.solambda.swiffer.api.internal.MethodInvoker;

public class ActivityExecutorImpl implements ActivityExecutor {
	private MethodInvoker invoker;
	private ArgumentsProvider argumentsProvider;

	public ActivityExecutorImpl(final MethodInvoker invoker, final ArgumentsProvider argumentsProvider) {
		super();
		Preconditions.checkNotNull(invoker, "MethodInvoker should not be null");
		this.argumentsProvider = argumentsProvider;
		this.invoker = invoker;
	}

	@Override
	public String execute(final ActivityTaskContext context) throws ActivityTaskExecutionFailedException {
		try {
			final Object[] arguments = this.argumentsProvider.getArguments(context);
			final Object result = this.invoker.invoke(arguments);
			if (result == null) {
				return null;
			}
			return result.toString();
		} catch (final InvocationTargetException e) {
			throw new ActivityTaskExecutionFailedException(context, e.getTargetException());
		}
	}

}
