package com.solambda.swiffer.api.internal.decisions;

import static com.solambda.swiffer.api.internal.decisions.ArgumentProviders.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.Control;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.EventHandlerCommonParameter;
import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.Marker;
import com.solambda.swiffer.api.Output;
import com.solambda.swiffer.api.Reason;
import com.solambda.swiffer.api.mapper.DataMapper;

public class EventHandlerArgumentsProviderFactory {
	private final DataMapper dataMapper;

	private static class InternalArgumentProvider {
		private BiFunction<EventContext, Decisions, Object> function;
		private boolean isDefaultProvider;

		public InternalArgumentProvider(final BiFunction<EventContext, Decisions, Object> function,
				final boolean isDefaultProvider) {
			super();
			this.function = function;
			this.isDefaultProvider = isDefaultProvider;
		}

	}

	public EventHandlerArgumentsProviderFactory(DataMapper dataMapper) {
		this.dataMapper = dataMapper;
	}

	public EventHandlerArgumentsProvider createArgumentsProvider(final EventType type, final Method method) {
		final Parameter[] parameters = method.getParameters();
		if (parameters.length == 0) {
			return (c, d) -> new Object[0];
		} else {
			final List<BiFunction<EventContext, Decisions, Object>> argumentProviders = new ArrayList<>();
			boolean defaultProviderFound = false;
			for (final Parameter parameter : parameters) {
				final InternalArgumentProvider argumentProvider = createArgumentProvider(type, parameter);
				if (argumentProvider.isDefaultProvider) {
					Preconditions.checkState(!defaultProviderFound,
							"Illegal event handler method %s. Only one non-annotated parameter is allowed. "
									+ "Please annotate other parameters with a %s annotation, like @Input,"
									+ "@Output,@Control,@Reason,@Details",
							method, EventHandlerCommonParameter.class);
					defaultProviderFound = true;
				}
				argumentProviders.add(argumentProvider.function);
			}
			return (c, d) -> {
				final Object[] arguments = new Object[parameters.length];
				int i = 0;
				for (final BiFunction<EventContext, Decisions, Object> function : argumentProviders) {
					arguments[i++] = function.apply(c, d);
				}
				return arguments;
			};
		}
	}

	private InternalArgumentProvider createArgumentProvider(
			final EventType eventType,
			final Parameter parameter) {
		final Class<?> parameterType = parameter.getType();
		BiFunction<EventContext, Decisions, Object> argumentProvider = null;
		argumentProvider = getArgumentProviderForSpecificParameterType(parameterType);
		if (argumentProvider != null) {
			return new InternalArgumentProvider(argumentProvider, false);
		}
		argumentProvider = getArgumentProviderForSpecificAnnotation(eventType, parameter);
		if (argumentProvider != null) {
			return new InternalArgumentProvider(argumentProvider, false);
		}
		return new InternalArgumentProvider(getDefaultArgumentProvider(eventType, parameterType), true);
	}

	/**
	 * Return an argument provider for a parameter annotated with a swiffer
	 * annotation, like @Input or @Marker
	 *
	 * @param eventType
	 *            the type of the event handler
	 * @param parameterType
	 *            the annotated type parameter
	 * @return
	 */
	private BiFunction<EventContext, Decisions, Object> getArgumentProviderForSpecificAnnotation(
			final EventType eventType, final AnnotatedElement parameterType) {
		if (parameterType.isAnnotationPresent(Input.class)) {
			return deserialize(INPUT_PROVIDER, parameterType);
		} else if (parameterType.isAnnotationPresent(Output.class)) {
			return deserialize(OUTPUT_PROVIDER, parameterType);
		} else if (parameterType.isAnnotationPresent(Control.class)) {
			return deserialize(CONTROL_PROVIDER, parameterType);
		} else if (parameterType.isAnnotationPresent(Reason.class)) {
			return wrapInBiFunction(REASON_PROVIDER);
		} else if (parameterType.isAnnotationPresent(Marker.class)) {
			return wrapInBiFunction(markerDetailsProvider(parameterType));
		}

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
	private BiFunction<EventContext, Decisions, Object> getArgumentProviderForSpecificParameterType(
			final Class<?> parameterType) {
		if (Decisions.class.isAssignableFrom(parameterType)) {
			return (c, d) -> d;
		} else if (DecisionTaskContext.class.isAssignableFrom(parameterType)) {
			return wrapInBiFunction(EVENT_CONTEXT_PROVIDER);
		} else if (WorkflowEvent.class.isAssignableFrom(parameterType)) {
			return wrapInBiFunction(EVENT_PROVIDER);
		} else if (WorkflowHistory.class.isAssignableFrom(parameterType)) {
			return wrapInBiFunction(WORKFLOW_HISTORY_PROVIDER);
		}
		return null;
	}

	private BiFunction<EventContext, Decisions, Object> wrapInBiFunction(
			final Function<EventContext, Object> eventContextProvider) {
		return (e, d) -> eventContextProvider.apply(e);
	}

	private BiFunction<EventContext, Decisions, Object> getDefaultArgumentProvider(final EventType eventType,
																				   final Class<?> argumentType) {
		// FIXME: check the compatibility of runtime value with the declared
		// parameter type (@runtime and also @build time)
		switch (eventType) {
			case ActivityTaskCompleted:
				return deserialize(OUTPUT_PROVIDER, argumentType);
			case TimerFired:
				return deserialize(CONTROL_PROVIDER, argumentType);
			case WorkflowExecutionSignaled:
			case WorkflowExecutionStarted:
				return deserialize(INPUT_PROVIDER, argumentType);
			case ActivityTaskTimedOut:
			case ActivityTaskFailed:
				return wrapInBiFunction(INITIAL_EVENT_ID_PROVIDER);
			case ChildWorkflowExecutionCanceled:
				return wrapInBiFunction(DETAILS_PROVIDER);
			case ChildWorkflowExecutionCompleted:
				return deserialize(OUTPUT_PROVIDER, argumentType);
			case ChildWorkflowExecutionFailed:
				return wrapInBiFunction(REASON_PROVIDER);
			case ChildWorkflowExecutionStarted:
				return wrapInBiFunction(CHILD_RUN_ID_PROVIDER);
			case ChildWorkflowExecutionTerminated:
				return wrapInBiFunction(INITIAL_EVENT_ID_PROVIDER);
			case ChildWorkflowExecutionTimedOut:
				return wrapInBiFunction(INITIAL_EVENT_ID_PROVIDER);
			case StartChildWorkflowExecutionFailed:
				return wrapInBiFunction(CAUSE_PROVIDER);
			case WorkflowExecutionCancelRequested:
				return wrapInBiFunction(CAUSE_PROVIDER);
			case TimerCanceled:
			case ActivityTaskCancelRequested:
			case ActivityTaskCanceled:
			case ActivityTaskScheduled:
			case ActivityTaskStarted:
			case CancelTimerFailed:
			case CancelWorkflowExecutionFailed:
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
			case StartChildWorkflowExecutionInitiated:
			case StartLambdaFunctionFailed:
			case StartTimerFailed:
			case TimerStarted:
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

	private BiFunction<EventContext, Decisions, Object> deserialize(Function<EventContext, String> provider, AnnotatedElement parameterType) {
		if (parameterType instanceof Parameter) {
			return deserialize(provider, ((Parameter) parameterType).getType());
		} else {
			return (eventContext, decisions) -> provider.apply(eventContext);
		}
	}

	private BiFunction<EventContext, Decisions, Object> deserialize(Function<EventContext, String> provider, Class<?> argumentType) {
		Function<EventContext, Object> inputProvider = context -> dataMapper.deserialize(provider.apply(context), argumentType);
		return (eventContext, decisions) -> inputProvider.apply(eventContext);
	}

	private Function<EventContext, Object> markerDetailsProvider(AnnotatedElement parameterType) {
		String markerName = parameterType.getAnnotation(Marker.class).value();
		return eventContext -> eventContext.getMarkerDetails(markerName, ((Parameter) parameterType).getType())
										   .orElse(null);
	}
}
