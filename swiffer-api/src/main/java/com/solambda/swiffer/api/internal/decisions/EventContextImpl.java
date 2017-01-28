package com.solambda.swiffer.api.internal.decisions;

import java.util.List;

import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.ActivityTaskCompletedContext;
import com.solambda.swiffer.api.internal.context.ActivityTaskFailedContext;
import com.solambda.swiffer.api.internal.context.ActivityTaskScheduleFailedContext;
import com.solambda.swiffer.api.internal.context.ActivityTaskTimedOutContext;
import com.solambda.swiffer.api.internal.context.MarkerRecordedContext;
import com.solambda.swiffer.api.internal.context.SignalReceivedContext;
import com.solambda.swiffer.api.internal.context.TimerCanceledContext;
import com.solambda.swiffer.api.internal.context.TimerFiredContext;
import com.solambda.swiffer.api.internal.context.WorkflowCancelRequestedContext;
import com.solambda.swiffer.api.internal.context.WorkflowStartedContext;
import com.solambda.swiffer.api.internal.context.WorkflowTerminatedContext;
import com.solambda.swiffer.api.internal.context.identifier.ContextName;
import com.solambda.swiffer.api.internal.context.identifier.MarkerName;
import com.solambda.swiffer.api.internal.context.identifier.SignalName;
import com.solambda.swiffer.api.internal.context.identifier.TaskName;
import com.solambda.swiffer.api.internal.context.identifier.TimerName;
import com.solambda.swiffer.api.internal.context.identifier.WorkflowName;
import com.solambda.swiffer.api.internal.events.EventCategory;

public class EventContextImpl implements
		EventContext,
		WorkflowStartedContext,
		WorkflowTerminatedContext,
		WorkflowCancelRequestedContext,
		SignalReceivedContext,
		ActivityTaskScheduleFailedContext,
		ActivityTaskCompletedContext,
		ActivityTaskFailedContext,
		ActivityTaskTimedOutContext,
		TimerFiredContext,
		TimerCanceledContext,
		MarkerRecordedContext {

	private DecisionTaskContext decisionContext;

	private WorkflowEvent event;

	public EventContextImpl(final DecisionTaskContext decisionContext, final WorkflowEvent event) {
		super();
		this.decisionContext = decisionContext;
		this.event = event;
	}

	@Override
	public WorkflowHistory history() {
		return this.decisionContext.history();
	}

	@Override
	public String taskToken() {
		return this.decisionContext.taskToken();
	}

	@Override
	public EventCategory category() {
		return this.event.category();
	}

	@Override
	public ContextName name() {
		switch (category()) {
		case ACTIVITY:
			return new TaskName(activityType());
		case SIGNAL:
			return new SignalName(signalName());
		case MARKER:
			return new MarkerName(markerName());
		case TIMER:
			return new TimerName(timerId());
		case DECISION:
		case WORKFLOW_EXECUTION:
			return new WorkflowName(workflowType());
		case LAMBDA://
		case SIGNAL_EXTERNAL_WORKFLOW:// signal + wfType(hard) ?
		case CANCEL_EXTERNAL_WORKFLOW:// wfId ?
		case CHILD_WORKFLOW:// should use child workflow type
		default:
			throw new IllegalArgumentException("cannot handle category id for " + category());
		}
	}

	@Override
	public List<WorkflowEvent> newEvents() {
		return this.decisionContext.newEvents();
	}

	@Override
	public String signalName() {
		return this.event.signalName();
	}

	@Override
	public String input() {
		return this.event.input();
	}

	@Override
	public String cause() {
		return this.event.cause();
	}

	@Override
	public String details() {
		return this.event.details();
	}

	@Override
	public String reason() {
		return this.event.reason();
	}

	@Override
	public String activityId() {
		return this.event.activityId();
	}

	@Override
	public VersionedName activityType() {
		return this.event.activityType();
	}

	@Override
	public String timerId() {
		return this.event.timerId();
	}

	@Override
	public String markerName() {
		return this.event.markerName();
	}

	@Override
	public String output() {
		return this.event.output();
	}

	@Override
	public String control() {
		return this.event.control();
	}

	@Override
	public WorkflowEvent event() {
		return this.event;
	}

	@Override
	public VersionedName workflowType() {
		return this.decisionContext.workflowType();
	}

}
