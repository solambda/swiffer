package com.solambda.swiffer.api.internal.decisions;

import java.util.function.Function;

public class ArgumentProviders {

	public static final Function<EventContext, Object> EVENT_CONTEXT_PROVIDER = c -> c;
	public static final Function<EventContext, Object> EVENT_PROVIDER = c -> c.event();
	public static final Function<EventContext, Object> WORKFLOW_HISTORY_PROVIDER = c -> c.history();

	// event properties
	public static final Function<EventContext, Object> ACTIVITY_ID_PROVIDER = c -> c.event().activityId();
	public static final Function<EventContext, Object> ACTIVITY_TYPE_PROVIDER = c -> c.event().activityType();
	public static final Function<EventContext, Object> CAUSE_PROVIDER = c -> c.event().cause();
	public static final Function<EventContext, Object> CONTROL_PROVIDER = c -> c.event().control();
	public static final Function<EventContext, Object> DETAILS_PROVIDER = c -> c.event().details();
	public static final Function<EventContext, Object> INPUT_PROVIDER = c -> c.event().input();
	public static final Function<EventContext, Object> MARKER_NAME_PROVIDER = c -> c.event().markerName();
	public static final Function<EventContext, Object> OUTPUT_PROVIDER = c -> c.event().output();
	public static final Function<EventContext, Object> REASON_PROVIDER = c -> c.event().reason();
	public static final Function<EventContext, Object> SIGNAL_NAME_PROVIDER = c -> c.event().signalName();
	public static final Function<EventContext, Object> TIMER_ID_PROVIDER = c -> c.event().timerId();

	public static Function<EventContext, Object> ensureArgumentAndThen(final Class<?> clazz,
			final Function<EventContext, Object> then) {

		return then;
	}

}
