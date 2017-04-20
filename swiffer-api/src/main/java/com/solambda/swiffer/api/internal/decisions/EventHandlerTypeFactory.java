package com.solambda.swiffer.api.internal.decisions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.*;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.identifier.ActivityName;
import com.solambda.swiffer.api.internal.context.identifier.ContextName;
import com.solambda.swiffer.api.internal.context.identifier.MarkerName;
import com.solambda.swiffer.api.internal.context.identifier.SignalName;
import com.solambda.swiffer.api.internal.context.identifier.TimerName;
import com.solambda.swiffer.api.internal.context.identifier.WorkflowName;
import com.solambda.swiffer.api.retry.RetryControl;

public class EventHandlerTypeFactory {

	private static final Map<Class<? extends Annotation>, EventType> EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE = new HashMap<>();
	static {
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnActivityCompleted.class, EventType.ActivityTaskCompleted);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnActivityFailed.class, EventType.ActivityTaskFailed);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnWorkflowStarted.class, EventType.WorkflowExecutionStarted);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnSignalReceived.class, EventType.WorkflowExecutionSignaled);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnTimerFired.class, EventType.TimerFired);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnMarkerRecorded.class, EventType.MarkerRecorded);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnRecordMarkerFailed.class, EventType.RecordMarkerFailed);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnChildWorkflowCanceled.class, EventType.ChildWorkflowExecutionCanceled);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnChildWorkflowCompleted.class, EventType.ChildWorkflowExecutionCompleted);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnChildWorkflowFailed.class, EventType.ChildWorkflowExecutionFailed);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnChildWorkflowTerminated.class, EventType.ChildWorkflowExecutionTerminated);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnChildWorkflowTimedOut.class, EventType.ChildWorkflowExecutionTimedOut);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnChildWorkflowStarted.class, EventType.ChildWorkflowExecutionStarted);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnStartChildWorkflowFailed.class, EventType.StartChildWorkflowExecutionFailed);
		EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.put(OnWorkflowCancelRequested.class, EventType.WorkflowExecutionCancelRequested);
	}

	private static interface ContextNameProvider<A extends Annotation> extends Function<A, ContextName> {

	}

	private static final Map<Class<? extends Annotation>, ContextNameProvider> map = new HashMap<>();
	{
		map.put(OnActivityCompleted.class, (ContextNameProvider<OnActivityCompleted>) this::toContextName);
		map.put(OnActivityFailed.class, (ContextNameProvider<OnActivityFailed>) this::toContextName);
		map.put(OnWorkflowStarted.class, (ContextNameProvider<OnWorkflowStarted>) this::toContextName);
		map.put(OnSignalReceived.class, (ContextNameProvider<OnSignalReceived>) this::toContextName);
		map.put(OnTimerFired.class, (ContextNameProvider<OnTimerFired>) this::toContextName);
		map.put(OnMarkerRecorded.class, (ContextNameProvider<OnMarkerRecorded>) this::toContextName);
		map.put(OnRecordMarkerFailed.class, (ContextNameProvider<OnRecordMarkerFailed>) this::toContextName);
		map.put(OnChildWorkflowCanceled.class, (ContextNameProvider<OnChildWorkflowCanceled>) this::toContextName);
		map.put(OnChildWorkflowCompleted.class, (ContextNameProvider<OnChildWorkflowCompleted>) this::toContextName);
		map.put(OnChildWorkflowFailed.class, (ContextNameProvider<OnChildWorkflowFailed>) this::toContextName);
		map.put(OnChildWorkflowTerminated.class, (ContextNameProvider<OnChildWorkflowTerminated>) this::toContextName);
		map.put(OnChildWorkflowTimedOut.class, (ContextNameProvider<OnChildWorkflowTimedOut>) this::toContextName);
		map.put(OnChildWorkflowStarted.class, (ContextNameProvider<OnChildWorkflowStarted>) this::toContextName);
		map.put(OnStartChildWorkflowFailed.class, (ContextNameProvider<OnStartChildWorkflowFailed>) this::toContextName);
		map.put(OnWorkflowCancelRequested.class, (ContextNameProvider<OnWorkflowCancelRequested>) this::toContextName);
	}

	private VersionedName workflowType;

	public EventHandlerTypeFactory(final VersionedName workflowType) {
		super();
		this.workflowType = workflowType;
	}

	public EventHandlerType create(final Method method) {
		final Annotation eventHandlerAnnotation = getEventHandlerAnnotation(method);
		if (eventHandlerAnnotation != null) {
			return processEventHandlerAnnotation(method, eventHandlerAnnotation);
		} else {
			return null;
		}
	}

	private EventHandlerType processEventHandlerAnnotation(final Method method,
			final Annotation eventHandlerAnnotation) {
		final EventType eventType = toEventType(eventHandlerAnnotation);
		final ContextName contextName = handlerToContextName(eventHandlerAnnotation);
		return new EventHandlerType(eventType, contextName);
	}

	@SuppressWarnings("unchecked")
	private ContextName handlerToContextName(final Annotation eventHandlerAnnotation) {
		final Class<? extends Annotation> eventHandlerAnnotationType = eventHandlerAnnotation.annotationType();
		final ContextNameProvider<Annotation> func = map.get(eventHandlerAnnotationType);
		return func.apply(eventHandlerAnnotation);
	}

	private Annotation getEventHandlerAnnotation(final Method method) {
		final Annotation[] annotations = method.getAnnotations();
		Annotation eventHandlerAnnotation = null;
		for (final Annotation annotation : annotations) {
			if (isEventHandlerAnnotation(annotation)) {
				Preconditions.checkState(eventHandlerAnnotation == null,
						"Method %s should not be annotated with more than one event handler annotation! ", method);
				eventHandlerAnnotation = annotation;
			}
		}
		return eventHandlerAnnotation;
	}

	private boolean isEventHandlerAnnotation(final Annotation annotation) {
		return annotation.annotationType().isAnnotationPresent(
				com.solambda.swiffer.api.EventHandler.class);
	}

	private EventType toEventType(final Annotation eventHandlerAnnotation) {
		final Class<? extends Annotation> annotationType = eventHandlerAnnotation.annotationType();
		final EventType eventType = EVENT_HANDLER_ANNOTATION_TO_EVENT_TYPE.get(annotationType);
		Preconditions.checkState(eventType != null, "No EventType associated with annotation %s",
				annotationType);
		return eventType;
	}

	// CONTEXT NAME METHODS //

	private ContextName toContextName(final OnActivityCompleted annotation) {
		final Class<?> activityDefinitionClass = annotation.value();
		return toActivityName(activityDefinitionClass);
	}

	private ContextName toContextName(final OnActivityFailed annotation) {
		final Class<?> activityDefinitionClass = annotation.activity();
		return toActivityName(activityDefinitionClass);
	}

	private ContextName toActivityName(final Class<?> activityDefinitionClass) {
		final ActivityType activityType = activityDefinitionClass.getAnnotation(ActivityType.class);
		Preconditions.checkState(activityType != null,
				"the activity definition %s should be annotated with @%s",
				activityDefinitionClass.getSimpleName(),
				ActivityType.class.getSimpleName());
		return new ActivityName(new VersionedName(activityType.name(), activityType.version()));
	}

	private ContextName toContextName(final OnWorkflowStarted annotation) {
		return new WorkflowName(this.workflowType);
	}

	private ContextName toContextName(final OnSignalReceived annotation) {
		return new SignalName(annotation.value());
	}

	private ContextName toContextName(final OnTimerFired annotation) {
		String timerId = checkTimerId(annotation.value());
		return new TimerName(timerId);
	}

	private ContextName toContextName(OnMarkerRecorded annotation) {
		return new MarkerName(annotation.value());
	}

	private ContextName toContextName(OnRecordMarkerFailed annotation) {
		return new MarkerName(annotation.value());
	}

	private ContextName toContextName(OnWorkflowCancelRequested annotation) {
		return new WorkflowName(workflowType);
	}

	private ContextName toContextName(OnChildWorkflowCanceled annotation) {
		return toChildWorkflowName(annotation.value());
	}

	private ContextName toContextName(OnChildWorkflowCompleted annotation) {
		return toChildWorkflowName(annotation.value());
	}

	private ContextName toContextName(OnChildWorkflowFailed annotation) {
		return toChildWorkflowName(annotation.value());
	}

	private ContextName toContextName(OnChildWorkflowTerminated annotation) {
		return toChildWorkflowName(annotation.value());
	}

	private ContextName toContextName(OnChildWorkflowTimedOut annotation) {
		return toChildWorkflowName(annotation.value());
	}

	private ContextName toContextName(OnChildWorkflowStarted annotation) {
		return toChildWorkflowName(annotation.value());
	}

	private ContextName toContextName(OnStartChildWorkflowFailed annotation) {
		return toChildWorkflowName(annotation.value());
	}

	private ContextName toChildWorkflowName(Class<?> annotation) {
		WorkflowType childWorkflow = annotation.getAnnotation(WorkflowType.class);
		return new WorkflowName(childWorkflow.name(), childWorkflow.version());
	}

	/**
	 * Ensure custom handler can not be specified for internal timer.
	 *
	 * @param timerId timer ID
	 * @return timer ID if acceptable
	 * @throws IllegalArgumentException if selected timer name is reserved for internal use
	 */
	private static String checkTimerId(String timerId) {
		Preconditions.checkNotNull(timerId);
		Preconditions.checkArgument(!timerId.startsWith(RetryControl.RETRY_TIMER), "This is reserved timer ID");

		return timerId;
	}
}
