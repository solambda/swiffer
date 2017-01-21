package com.solambda.swiffer.api.model.decider;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.model.decider.context.*;

public class ContextTypeMap {
	private static Map<EventType, Class<? extends EventContext>> map = new HashMap<EventType, Class<? extends EventContext>>();

	static {
		map.put(EventType.ScheduleActivityTaskFailed, TaskScheduleFailedContext.class);
		map.put(EventType.ActivityTaskCompleted, TaskCompletedContext.class);
		map.put(EventType.ActivityTaskFailed, TaskFailedContext.class);
		map.put(EventType.ActivityTaskTimedOut, TaskTimedOutContext.class);
		map.put(EventType.TimerFired, TimerFiredContext.class);
		map.put(EventType.TimerCanceled, TimerCanceledContext.class);
		map.put(EventType.WorkflowExecutionCancelRequested, WorkflowCancelRequestedContext.class);
		map.put(EventType.WorkflowExecutionStarted, WorkflowStartedContext.class);
		map.put(EventType.WorkflowExecutionTerminated, WorkflowTerminatedContext.class);
		map.put(EventType.MarkerRecorded, MarkerRecordedContext.class);
		map.put(EventType.WorkflowExecutionSignaled, SignalReceivedContext.class);
	}

	public static Class<? extends EventContext> contextClass(final EventType type) {
		return map.get(type);
	}
}
