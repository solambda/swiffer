package com.solambda.swiffer.api.internal.decisions;

import java.util.List;
import java.util.Optional;

import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.*;
import com.solambda.swiffer.api.internal.context.identifier.ActivityName;
import com.solambda.swiffer.api.internal.context.identifier.ContextName;
import com.solambda.swiffer.api.internal.context.identifier.MarkerName;
import com.solambda.swiffer.api.internal.context.identifier.SignalName;
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
		MarkerRecordedContext,
		ChildWorkflowContext,
		ExternalWorkflowContext {

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
				return new ActivityName(activityType());
			case SIGNAL:
				return new SignalName(signalName());
			case MARKER:
				return new MarkerName(markerName());
			case TIMER:
				return new TimerName(timerId());
			case DECISION:
			case WORKFLOW_EXECUTION:
				return new WorkflowName(workflowType());
			case CANCEL_EXTERNAL_WORKFLOW:
				return new WorkflowName(workflowType());
			case CHILD_WORKFLOW:
				return childWorkflowName();
			case LAMBDA://
			case SIGNAL_EXTERNAL_WORKFLOW:// signal + wfType(hard) ?
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

	@Override
	public boolean hasMarker(String markerName) {
		return decisionContext.hasMarker(markerName);
	}

	@Override
	public <T> Optional<T> getMarkerDetails(String markerName, Class<T> type) {
		return decisionContext.getMarkerDetails(markerName, type);
	}

	@Override
	public WorkflowName childWorkflowName(){
		WorkflowType workflowType = event.childWorkflowType();
		return new WorkflowName(workflowType.getName(), workflowType.getVersion());
	}

	@Override
	public String getExternalWorkflowId() {
		return event.getExternalWorkflowId();
	}

	@Override
	public String getExternalWorkflowRunId() {
		return event.getExternalWorkflowRunId();
	}
}
