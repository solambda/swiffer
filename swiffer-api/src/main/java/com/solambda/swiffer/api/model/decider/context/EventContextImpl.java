package com.solambda.swiffer.api.model.decider.context;

import java.util.List;

import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.model.*;
import com.solambda.swiffer.api.model.decider.ContextTypeMap;
import com.solambda.swiffer.api.model.decider.DecisionContext;
import com.solambda.swiffer.api.model.decider.WorkflowEvent;
import com.solambda.swiffer.api.model.decider.context.identifier.*;

public class EventContextImpl implements
		EventContext,
		WorkflowStartedContext,
		WorkflowTerminatedContext,
		WorkflowCancelRequestedContext,
		SignalReceivedContext,
		TaskScheduleFailedContext,
		TaskCompletedContext,
		TaskFailedContext,
		TaskTimedOutContext,
		TimerFiredContext,
		TimerCanceledContext,
		MarkerRecordedContext
{

	private DecisionContext decisionContext;

	private WorkflowEvent event;

	public EventContextImpl(final DecisionContext decisionContext, final WorkflowEvent event) {
		super();
		this.decisionContext = decisionContext;
		this.event = event;
	}

	@Override
	public WorkflowHistory history() {
		return decisionContext.history();
	}

	@Override
	public String decisionTaskId() {
		return decisionContext.decisionTaskId();
	}

	@Override
	public ContextCategory category() {
		return event.category();
	}

	@Override
	public ContextName name() {
		switch (category()) {
		case ACTIVITY:
			return new TaskName(taskType());
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
		return decisionContext.newEvents();
	}

	@Override
	public String signalName() {
		return event.signalName();
	}

	@Override
	public String input() {
		return event.input();
	}

	@Override
	public String cause() {
		return event.cause();
	}

	@Override
	public String details() {
		return event.details();
	}

	@Override
	public String reason() {
		return event.reason();
	}

	@Override
	public String taskId() {
		return event.taskId();
	}

	@Override
	public VersionedName taskType() {
		return event.taskType();
	}

	@Override
	public String timerId() {
		return event.timerId();
	}

	@Override
	public String markerName() {
		return event.markerName();
	}

	@Override
	public String output() {
		return event.output();
	}

	@Override
	public String control() {
		return event.control();
	}

	@Override
	public WorkflowTypeId workflowType() {
		return decisionContext.workflowType();
	}

	@Override
	public ContextType contextType() {
		return new ContextType(ContextTypeMap.contextClass(event.type()));
	}

}
