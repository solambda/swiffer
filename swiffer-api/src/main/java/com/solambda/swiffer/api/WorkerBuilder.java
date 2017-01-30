package com.solambda.swiffer.api;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.internal.MethodInvoker;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.WorkerImpl;
import com.solambda.swiffer.api.internal.activities.ActivityExecutionReporter;
import com.solambda.swiffer.api.internal.activities.ActivityExecutionReporterImpl;
import com.solambda.swiffer.api.internal.activities.ActivityExecutor;
import com.solambda.swiffer.api.internal.activities.ActivityExecutorArgumentsProvider;
import com.solambda.swiffer.api.internal.activities.ActivityExecutorImpl;
import com.solambda.swiffer.api.internal.activities.ActivityExecutorRegistry;
import com.solambda.swiffer.api.internal.activities.ActivityTaskContext;
import com.solambda.swiffer.api.internal.activities.ActivityTaskPoller;
import com.solambda.swiffer.api.internal.registration.ActivityTypeRegistry;

public class WorkerBuilder {

	private AmazonSimpleWorkflow swf;
	private String domain;
	private String identity;
	private String taskList;
	private List<Object> executors;
	private ActivityTypeRegistry activityTypeRegistry;

	public WorkerBuilder(final AmazonSimpleWorkflow swf, final String domain) {
		super();
		this.swf = swf;
		this.domain = domain;
		this.activityTypeRegistry = new ActivityTypeRegistry(swf, domain);
	}

	public Worker build() {
		final ActivityExecutorRegistry registry = createExecutorRegistry();
		final ActivityTaskPoller poller = new ActivityTaskPoller(
				this.swf,
				this.domain,
				this.taskList,
				this.identity);
		final ActivityExecutionReporter reporter = new ActivityExecutionReporterImpl(this.swf);
		return new WorkerImpl(poller, registry, reporter);
	}

	private ActivityExecutorRegistry createExecutorRegistry() {
		final Map<VersionedName, ActivityExecutor> registry = new HashMap<>();
		fillRegistryByIntrospectingExecutors(registry);
		if (registry.isEmpty()) {
			throw new IllegalStateException("no executors found in executors: " + this.executors);
		}
		return new ActivityExecutorRegistry(registry);
	}

	private void fillRegistryByIntrospectingExecutors(final Map<VersionedName, ActivityExecutor> registry) {
		for (final Object executor : this.executors) {
			fillRegistryByIntrospectingExecutor(registry, executor);
		}
	}

	private void fillRegistryByIntrospectingExecutor(final Map<VersionedName, ActivityExecutor> registry,
			final Object executorClassInstance) {
		final Class<? extends Object> executorClass = executorClassInstance.getClass();
		final Method[] publicMethods = executorClass.getMethods();
		for (final Method publicMethod : publicMethods) {
			final Executor executorAnnotation = publicMethod.getAnnotation(Executor.class);
			if (executorAnnotation != null) {
				final Class<?> activity = executorAnnotation.activity();
				final ActivityType activityTypeAnnotation = validateActivityParameter(activity);
				this.activityTypeRegistry.registerActivityOrCheckConfiguration(activityTypeAnnotation);
				fillRegistryForMethod(registry, executorClassInstance, publicMethod, activityTypeAnnotation);
			}
		}
	}

	private ActivityType validateActivityParameter(final Class<?> activityTypeClass) {
		Preconditions.checkArgument(activityTypeClass.isInterface());
		final ActivityType activityType = activityTypeClass.getAnnotation(ActivityType.class);
		Preconditions.checkState(activityType != null, "The interface %s, should be annotated with %s!",
				activityTypeClass, ActivityType.class);
		return activityType;
	}

	private void fillRegistryForMethod(
			final Map<VersionedName, ActivityExecutor> registry,
			final Object executorClassInstance,
			final Method publicMethod,
			final ActivityType annotation) {
		final VersionedName key = new VersionedName(annotation.name(), annotation.version());
		final MethodInvoker invoker = new MethodInvoker(executorClassInstance, publicMethod);
		final ActivityExecutorArgumentsProvider argumentsProvider = createArgumentsProvider(
				publicMethod);
		final ActivityExecutor value = new ActivityExecutorImpl(invoker, argumentsProvider);
		registry.put(key, value);
	}

	public static void main(final String[] args) {
		System.out.println(1 << 0);
		System.out.println(1 << 1);
		System.out.println(1 << 2);
		System.out.println(1 << 3);
	}

	private ActivityExecutorArgumentsProvider createArgumentsProvider(final Method publicMethod) {
		final AnnotatedType[] parameterTypes = publicMethod.getAnnotatedParameterTypes();
		if (parameterTypes.length == 0) {
			return (c) -> new Object[0];
		} else {
			final List<Function<ActivityTaskContext, Object>> argumentProviders = new ArrayList<Function<ActivityTaskContext, Object>>();
			for (final AnnotatedType annotatedType : parameterTypes) {
				final Function<ActivityTaskContext, Object> argumentProvider = createArgumentProvider(annotatedType);
				argumentProviders.add(argumentProvider);
			}
			return (c) -> {
				final Object[] arguments = new Object[parameterTypes.length];
				int i = 0;
				for (final Function<ActivityTaskContext, Object> function : argumentProviders) {
					arguments[i++] = function.apply(c);
				}
				return arguments;
			};
		}
	}

	private Function<ActivityTaskContext, Object> createArgumentProvider(final AnnotatedType annotatedType) {
		final Type type = annotatedType.getType();
		if (type instanceof Class) {
			// final Class<?> parameterType = (Class) type;
			// FIXME: handle ActivityTaskContext parameter here!
			// FIXME: a parameter annotated with @GetState trigger state
			// retrieval
			// default is to get the activitytask input
			return (c) -> {
				// FIXME: do deserialization
				final String input = c.input();
				if (input == null) {
					// WARN with appropriate logger: an ninput was expected byt
					// null was received
				}
				return input;
			};
		} else {
			throw new IllegalStateException("cannot create argument resolver for parameter of type " + annotatedType);
		}
	}

	/**
	 * @param taskList
	 * @return
	 */
	public WorkerBuilder taskList(final String taskList) {
		this.taskList = taskList;
		return this;
	}

	public WorkerBuilder identity(final String identity) {
		this.identity = identity;
		return this;
	}

	public WorkerBuilder executors(final Object... executors) {
		this.executors = Arrays.asList(executors);
		return this;
	}
}
