package com.solambda.swiffer.api.internal.decisions;

import static com.solambda.swiffer.api.internal.decisions.ArgumentProviders.CONTROL_PROVIDER;
import static com.solambda.swiffer.api.internal.decisions.ArgumentProviders.EVENT_CONTEXT_PROVIDER;
import static com.solambda.swiffer.api.internal.decisions.ArgumentProviders.EVENT_PROVIDER;
import static com.solambda.swiffer.api.internal.decisions.ArgumentProviders.INPUT_PROVIDER;
import static com.solambda.swiffer.api.internal.decisions.ArgumentProviders.OUTPUT_PROVIDER;
import static com.solambda.swiffer.api.internal.decisions.ArgumentProviders.REASON_PROVIDER;
import static com.solambda.swiffer.api.internal.decisions.ArgumentProviders.WORKFLOW_HISTORY_PROVIDER;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.OnActivityCompleted;
import com.solambda.swiffer.api.internal.ArgumentsProvider;
import com.solambda.swiffer.api.internal.MethodInvoker;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.identifier.ContextName;
import com.solambda.swiffer.api.internal.context.identifier.TaskName;

public class WorkflowTemplateFactory {

	/**
	 * @param template
	 *            create a {@link WorkflowTemplate} instance by introspecting a
	 *            user defined template containing annotation based event
	 *            handlers.
	 *            <p>
	 * @return
	 */
	public WorkflowTemplate create(final Object template) {

		final EventHandlerRegistry eventHandlerRegistry = createEventHandlerRegistry(template);
		return new WorkflowTemplateImpl(eventHandlerRegistry);
	}

	private EventHandlerRegistry createEventHandlerRegistry(final Object template) {
		final Map<EventHandlerType, EventHandler> registry = new HashMap<>();
		fillRegistryByIntrospectingTemplateMethods(template, registry);
		return new EventHandlerRegistry(registry);
	}

	private void fillRegistryByIntrospectingTemplateMethods(final Object template,
			final Map<EventHandlerType, EventHandler> registry) {
		// find all methods annotated with an annotation which is annotated with
		// EventHandler
		final Method[] methods = template.getClass().getMethods();
		for (final Method method : methods) {
			final Annotation[] annotations = method.getAnnotations();
			for (final Annotation annotation : annotations) {
				final EventHandlerType type = toEventHandlerType(annotation);
				if (type != null) {
					final EventHandler handler = createEventHandler(template, type, method);
					registry.put(type, handler);
				}
			}
		}

		// assert not 2 times the same EventHandlerType

	}

	private EventHandler createEventHandler(final Object template, final EventHandlerType type, final Method method) {
		final MethodInvoker methodInvoker = new MethodInvoker(template, method);
		// verify the arguments are ok,
		final ArgumentsProvider<EventContext> argumentsProvider = createArgumentsProvider(type, method);
		// and the return value also
		// checkReturnType(method);
		// TODO Verify the exceptions are ok
		return new EventHandlerImpl(methodInvoker, argumentsProvider);
	}

	private ArgumentsProvider<EventContext> createArgumentsProvider(final EventHandlerType type, final Method method) {
		final AnnotatedType[] parameterTypes = method.getAnnotatedParameterTypes();
		if (parameterTypes.length == 0) {
			return (c) -> new Object[0];
		} else {
			final List<Function<EventContext, Object>> argumentProviders = new ArrayList<Function<EventContext, Object>>();
			for (final AnnotatedType annotatedType : parameterTypes) {
				final Function<EventContext, Object> argumentProvider = createArgumentProvider(type, annotatedType);
				argumentProviders.add(argumentProvider);
			}
			return (c) -> {
				final Object[] arguments = new Object[parameterTypes.length];
				int i = 0;
				for (final Function<EventContext, Object> function : argumentProviders) {
					arguments[i++] = function.apply(c);
				}
				return arguments;
			};
		}
	}

	private Function<EventContext, Object> createArgumentProvider(
			final EventHandlerType eventHandlerType,
			final AnnotatedType annotatedParameterType) {
		final Class<?> parameterType = (Class<?>) annotatedParameterType.getType();
		Function<EventContext, Object> argumentProvider = null;
		argumentProvider = getArgumentProviderForSpecificArgumentType(parameterType);
		if (argumentProvider != null) {
			return argumentProvider;
		}
		argumentProvider = getArgumentProviderForSpecificAnnotation(eventHandlerType, annotatedParameterType);
		if (argumentProvider != null) {
			return argumentProvider;
		}
		return getDefaultArgumentProvider(eventHandlerType, parameterType);
	}

	/**
	 * Return an argument provider for a parameter annotated with a swiffer
	 * annotation, like @Input or @Marker
	 *
	 * @param eventHandlerType
	 *            the type of the event handler
	 * @param parameterType
	 *            the annotated type parameter
	 * @return
	 */
	private Function<EventContext, Object> getArgumentProviderForSpecificAnnotation(
			final EventHandlerType eventHandlerType, final AnnotatedType parameterType) {
		// TODO: handle @Marker
		// TODO: handle other annotations like @Input, @Output etc...
		// TODO: check the validity of the annotation against the event type (
		// for instance, @Input is illegal on a TimerFired event)
		return null;
	}

	/**
	 * Return an argument provider for a parameter of a specific type, like
	 * {@link WorkflowHistory} etc...
	 *
	 * @param parameterType
	 * @return
	 */
	private Function<EventContext, Object> getArgumentProviderForSpecificArgumentType(final Class<?> parameterType) {
		if (parameterType.isAssignableFrom(DecisionTaskContext.class)) {
			return EVENT_CONTEXT_PROVIDER;
		} else if (parameterType.isAssignableFrom(WorkflowEvent.class)) {
			return EVENT_PROVIDER;
		} else if (parameterType.isAssignableFrom(WorkflowHistory.class)) {
			return WORKFLOW_HISTORY_PROVIDER;
		}
		return null;
	}

	private Function<EventContext, Object> getDefaultArgumentProvider(final EventHandlerType eventHandlerType,
			final Class<?> argumentType) {
		// FIXME: check the compatibility of runtime value with the declared
		// parameter type (@runtime and also @build time)

		final EventType eventType = eventHandlerType.getEventType();
		switch (eventType) {
		case ActivityTaskCompleted:
			return OUTPUT_PROVIDER;
		case TimerFired:
			return CONTROL_PROVIDER;
		case WorkflowExecutionSignaled:
		case WorkflowExecutionStarted:
			return INPUT_PROVIDER;
		case ActivityTaskFailed:
			return REASON_PROVIDER;

		case TimerCanceled:
		case ActivityTaskTimedOut:
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case ActivityTaskScheduled:
		case ActivityTaskStarted:
		case CancelTimerFailed:
		case CancelWorkflowExecutionFailed:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case CompleteWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
		case ExternalWorkflowExecutionCancelRequested:
		case ExternalWorkflowExecutionSignaled:
		case FailWorkflowExecutionFailed:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case MarkerRecorded:
		case RecordMarkerFailed:
		case RequestCancelActivityTaskFailed:
		case RequestCancelExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case ScheduleActivityTaskFailed:
		case ScheduleLambdaFunctionFailed:
		case SignalExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionInitiated:
		case StartChildWorkflowExecutionFailed:
		case StartChildWorkflowExecutionInitiated:
		case StartLambdaFunctionFailed:
		case StartTimerFailed:
		case TimerStarted:
		case WorkflowExecutionCancelRequested:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
		default:
			throw new IllegalStateException("not yet implemented");
		}
	}

	private void checkReturnType(final Method method) {
		final Class<?> returnType = method.getReturnType();
		// return type must be either:
		// - void
		// - Decisions
		// - Collection<Decision>
		// - any other type is considered as an output
	}

	private EventHandlerType toEventHandlerType(final Annotation annotation) {
		final Class<? extends Annotation> annotationType = annotation.annotationType();
		if (annotationType.isAnnotationPresent(com.solambda.swiffer.api.EventHandler.class)) {
			if (OnActivityCompleted.class.isAssignableFrom(annotationType)) {
				return new EventHandlerType(EventType.ActivityTaskCompleted,
						toContextName((OnActivityCompleted) annotation));
			} else {
				throw new IllegalStateException("cannot convert from " + annotationType + " to a EventHandlerType");
			}
		}
		return null;
	}

	private ContextName toContextName(final OnActivityCompleted annotation) {
		final Class<?> activityDefinitionClass = annotation.activity();
		final ActivityType activityType = toActivityType(activityDefinitionClass);
		return new TaskName(new VersionedName(activityType.name(), activityType.version()));
	}

	private ActivityType toActivityType(final Class<?> activityDefinition) {
		final ActivityType annotation = activityDefinition.getAnnotation(ActivityType.class);
		Preconditions.checkState(annotation != null,
				"the activity definition %s should be annotated with @%s",
				activityDefinition.getSimpleName(),
				ActivityType.class.getSimpleName());
		return annotation;
	}

}
