package com.solambda.swiffer.api.internal.decisions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.OnActivityCompleted;
import com.solambda.swiffer.api.OnActivityFailed;
import com.solambda.swiffer.api.OnMarkerRecorded;
import com.solambda.swiffer.api.OnRecordMarkerFailed;
import com.solambda.swiffer.api.OnSignalReceived;
import com.solambda.swiffer.api.OnTimerFired;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.identifier.ActivityName;
import com.solambda.swiffer.api.internal.context.identifier.ContextName;
import com.solambda.swiffer.api.internal.context.identifier.MarkerName;
import com.solambda.swiffer.api.internal.context.identifier.SignalName;
import com.solambda.swiffer.api.internal.context.identifier.TimerName;
import com.solambda.swiffer.api.internal.context.identifier.WorkflowName;

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
		return new TimerName(annotation.value());
	}

	private ContextName toContextName(OnMarkerRecorded annotation) {
		return new MarkerName(annotation.value());
	}

	private ContextName toContextName(OnRecordMarkerFailed annotation) {
		return new MarkerName(annotation.value());
	}
}
