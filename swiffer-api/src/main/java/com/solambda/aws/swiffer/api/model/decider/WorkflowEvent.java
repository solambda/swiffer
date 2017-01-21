package com.solambda.aws.swiffer.api.model.decider;

import static com.solambda.aws.swiffer.api.model.ContextCategory.ACTIVITY;
import static com.solambda.aws.swiffer.api.model.ContextCategory.CANCEL_EXTERNAL_WORKFLOW;
import static com.solambda.aws.swiffer.api.model.ContextCategory.CHILD_WORKFLOW;
import static com.solambda.aws.swiffer.api.model.ContextCategory.DECISION;
import static com.solambda.aws.swiffer.api.model.ContextCategory.LAMBDA;
import static com.solambda.aws.swiffer.api.model.ContextCategory.MARKER;
import static com.solambda.aws.swiffer.api.model.ContextCategory.SIGNAL;
import static com.solambda.aws.swiffer.api.model.ContextCategory.SIGNAL_EXTERNAL_WORKFLOW;
import static com.solambda.aws.swiffer.api.model.ContextCategory.TIMER;
import static com.solambda.aws.swiffer.api.model.ContextCategory.WORKFLOW_EXECUTION;
import static com.solambda.aws.swiffer.api.model.WorkflowEventState.ACTIVE;
import static com.solambda.aws.swiffer.api.model.WorkflowEventState.CANCELED;
import static com.solambda.aws.swiffer.api.model.WorkflowEventState.ERROR;
import static com.solambda.aws.swiffer.api.model.WorkflowEventState.INITIAL;
import static com.solambda.aws.swiffer.api.model.WorkflowEventState.SUCCESS;
import static com.solambda.aws.swiffer.api.model.WorkflowEventState.TIMEOUT;

import java.time.Instant;

import com.amazonaws.services.simpleworkflow.model.ActivityType;
import com.amazonaws.services.simpleworkflow.model.EventType;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.solambda.aws.swiffer.api.model.*;

public class WorkflowEvent implements Comparable<WorkflowEvent> {

	private HistoryEvent event;
	private WorkflowHistory history;

	public WorkflowEvent(final HistoryEvent event, final WorkflowHistory history) {
		super();
		this.event = event;
		this.history = history;
	}

	public EventType type() {
		return EventType.fromValue(event.getEventType());
	}

	private HistoryEvent historyEvent() {
		return event;
	}

	public Long id() {
		return historyEvent().getEventId();
	}

	public Instant eventTimestamp() {
		return historyEvent().getEventTimestamp().toInstant();
	}

	public ContextCategory category() {
		switch (type()) {
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskScheduled:
		case ActivityTaskStarted:
		case ActivityTaskTimedOut:
		case RequestCancelActivityTaskFailed:
		case ScheduleActivityTaskFailed:
			return ACTIVITY;
		case StartTimerFailed:
		case TimerCanceled:
		case TimerFired:
		case TimerStarted:
		case CancelTimerFailed:
			return TIMER;
		case WorkflowExecutionCancelRequested:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionFailed:
		case WorkflowExecutionStarted:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
		case CancelWorkflowExecutionFailed:
		case CompleteWorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionContinuedAsNew:
			return WORKFLOW_EXECUTION;
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case StartChildWorkflowExecutionFailed:
		case StartChildWorkflowExecutionInitiated:
			return CHILD_WORKFLOW;
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
			return DECISION;
		case ExternalWorkflowExecutionCancelRequested:
		case RequestCancelExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
			return CANCEL_EXTERNAL_WORKFLOW;
		case ExternalWorkflowExecutionSignaled:
		case SignalExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionInitiated:
			return SIGNAL_EXTERNAL_WORKFLOW;
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case ScheduleLambdaFunctionFailed:
		case StartLambdaFunctionFailed:
			return LAMBDA;
		case MarkerRecorded:
		case RecordMarkerFailed:
			return MARKER;
		case WorkflowExecutionSignaled:
			return SIGNAL;
		default:
			throw new IllegalArgumentException("cannot handle category for " + type());
		}
	}

	public String signalName() {
		switch (type()) {
		case SignalExternalWorkflowExecutionInitiated:
			return historyEvent().getSignalExternalWorkflowExecutionInitiatedEventAttributes().getSignalName();
		case WorkflowExecutionSignaled:
			return historyEvent().getWorkflowExecutionSignaledEventAttributes().getSignalName();
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskScheduled:
		case ActivityTaskStarted:
		case ActivityTaskTimedOut:
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
		case StartChildWorkflowExecutionFailed:
		case StartChildWorkflowExecutionInitiated:
		case StartLambdaFunctionFailed:
		case StartTimerFailed:
		case TimerCanceled:
		case TimerFired:
		case TimerStarted:
		case WorkflowExecutionCancelRequested:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionStarted:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public WorkflowEventState state() {
		switch (type()) {
		case WorkflowExecutionStarted:
		case DecisionTaskScheduled:
		case ActivityTaskScheduled:
		case LambdaFunctionScheduled:
		case MarkerRecorded:
		case TimerStarted:
		case StartChildWorkflowExecutionInitiated:
		case SignalExternalWorkflowExecutionInitiated:
		case WorkflowExecutionContinuedAsNew:
		case RequestCancelExternalWorkflowExecutionInitiated:
			return INITIAL;
		case WorkflowExecutionCancelRequested:
		case DecisionTaskStarted:
		case ChildWorkflowExecutionStarted:
		case ExternalWorkflowExecutionCancelRequested:
		case ActivityTaskStarted:
		case ActivityTaskCancelRequested:
		case LambdaFunctionStarted:
			return ACTIVE;
		case WorkflowExecutionCompleted:
		case ActivityTaskCompleted:
		case DecisionTaskCompleted:
		case ChildWorkflowExecutionCompleted:
		case TimerFired:
		case WorkflowExecutionSignaled:
		case ExternalWorkflowExecutionSignaled:
		case LambdaFunctionCompleted:
		case WorkflowExecutionTerminated:
		case ChildWorkflowExecutionTerminated:
			return SUCCESS;
		case CompleteWorkflowExecutionFailed:
		case WorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case CancelWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case ActivityTaskFailed:
		case RequestCancelActivityTaskFailed:
		case ScheduleActivityTaskFailed:
		case StartTimerFailed:
		case CancelTimerFailed:
		case RecordMarkerFailed:
		case RequestCancelExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionFailed:
		case StartChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionFailed:
		case LambdaFunctionFailed:
		case StartLambdaFunctionFailed:
		case ScheduleLambdaFunctionFailed:
			return ERROR;
		case WorkflowExecutionTimedOut:
		case ChildWorkflowExecutionTimedOut:
		case ActivityTaskTimedOut:
		case DecisionTaskTimedOut:
			return TIMEOUT;
		case WorkflowExecutionCanceled:
		case ActivityTaskCanceled:
		case TimerCanceled:
		case ChildWorkflowExecutionCanceled:
		case LambdaFunctionTimedOut:
			return CANCELED;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public String input() {
		switch (type()) {

		case WorkflowExecutionStarted:
			return historyEvent().getWorkflowExecutionStartedEventAttributes().getInput();
		case WorkflowExecutionContinuedAsNew:
			return historyEvent().getWorkflowExecutionContinuedAsNewEventAttributes().getInput();
		case ActivityTaskScheduled:
			return historyEvent().getActivityTaskScheduledEventAttributes().getInput();
		case WorkflowExecutionSignaled:
			return historyEvent().getWorkflowExecutionSignaledEventAttributes().getInput();
		case MarkerRecorded:
			return historyEvent().getMarkerRecordedEventAttributes().getDetails();
		case TimerStarted:
			return "Timer Started";
		case StartChildWorkflowExecutionInitiated:
			return historyEvent().getStartChildWorkflowExecutionInitiatedEventAttributes().getInput();
		case SignalExternalWorkflowExecutionInitiated:
			return historyEvent().getSignalExternalWorkflowExecutionInitiatedEventAttributes().getInput();
		case LambdaFunctionScheduled:
			return historyEvent().getLambdaFunctionScheduledEventAttributes().getInput();
		case WorkflowExecutionCancelRequested:
		case WorkflowExecutionCompleted:
		case CompleteWorkflowExecutionFailed:
		case WorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case WorkflowExecutionTimedOut:
		case WorkflowExecutionCanceled:
		case CancelWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionTerminated:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskCompleted:
		case ScheduleActivityTaskFailed:
		case ActivityTaskStarted:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskTimedOut:
		case ActivityTaskCanceled:
		case ActivityTaskCancelRequested:
		case RequestCancelActivityTaskFailed:
		case RecordMarkerFailed:
		case StartTimerFailed:
		case TimerFired:
		case TimerCanceled:
		case CancelTimerFailed:
		case StartChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionTimedOut:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionTerminated:
		case SignalExternalWorkflowExecutionFailed:
		case ExternalWorkflowExecutionSignaled:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case RequestCancelExternalWorkflowExecutionFailed:
		case ExternalWorkflowExecutionCancelRequested:
		case DecisionTaskTimedOut:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionTimedOut:
		case ScheduleLambdaFunctionFailed:
		case StartLambdaFunctionFailed:
		case LambdaFunctionStarted:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public String control() {
		switch (type()) {
		case TimerStarted:
			return historyEvent().getTimerStartedEventAttributes().getControl();
		case TimerFired:
			return initialEvent().control();
		case TimerCanceled:
			return initialEvent().control();
		case ActivityTaskScheduled:
			return historyEvent().getActivityTaskScheduledEventAttributes().getControl();
		case StartChildWorkflowExecutionInitiated:
			return historyEvent().getStartChildWorkflowExecutionInitiatedEventAttributes().getControl();
		case StartChildWorkflowExecutionFailed:
			return historyEvent().getStartChildWorkflowExecutionFailedEventAttributes().getControl();
		case SignalExternalWorkflowExecutionInitiated:
			return historyEvent().getSignalExternalWorkflowExecutionInitiatedEventAttributes().getControl();
		case SignalExternalWorkflowExecutionFailed:
			return historyEvent().getSignalExternalWorkflowExecutionFailedEventAttributes().getControl();
		case RequestCancelExternalWorkflowExecutionInitiated:
			return historyEvent().getRequestCancelExternalWorkflowExecutionInitiatedEventAttributes().getControl();
		case RequestCancelExternalWorkflowExecutionFailed:
			return historyEvent().getRequestCancelExternalWorkflowExecutionFailedEventAttributes().getControl();
		case WorkflowExecutionStarted:
		case WorkflowExecutionCancelRequested:
		case WorkflowExecutionCompleted:
		case CompleteWorkflowExecutionFailed:
		case WorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case WorkflowExecutionTimedOut:
		case WorkflowExecutionCanceled:
		case CancelWorkflowExecutionFailed:
		case WorkflowExecutionContinuedAsNew:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionTerminated:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskCompleted:
		case DecisionTaskTimedOut:
		case ScheduleActivityTaskFailed:
		case ActivityTaskStarted:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskTimedOut:
		case ActivityTaskCanceled:
		case ActivityTaskCancelRequested:
		case RequestCancelActivityTaskFailed:
		case WorkflowExecutionSignaled:
		case MarkerRecorded:
		case RecordMarkerFailed:
		case StartTimerFailed:

		case CancelTimerFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionTimedOut:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionTerminated:
		case ExternalWorkflowExecutionSignaled:
		case ExternalWorkflowExecutionCancelRequested:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case ScheduleLambdaFunctionFailed:
		case StartLambdaFunctionFailed:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public String output() {
		switch (type()) {

		case WorkflowExecutionCompleted:
			return historyEvent().getWorkflowExecutionCompletedEventAttributes().getResult();
		case DecisionTaskCompleted:
			return historyEvent().getDecisionTaskCompletedEventAttributes().getExecutionContext();
		case ActivityTaskCompleted:
			return historyEvent().getActivityTaskCompletedEventAttributes().getResult();
		case WorkflowExecutionSignaled:
			return historyEvent().getWorkflowExecutionSignaledEventAttributes().getInput();
		case MarkerRecorded:
			return historyEvent().getMarkerRecordedEventAttributes().getDetails();
		case TimerFired:
			return "Timer Fired";
		case TimerCanceled:
			return "Timer Canceled";
		case ChildWorkflowExecutionCompleted:
			return historyEvent().getChildWorkflowExecutionCompletedEventAttributes().getResult();
		case ExternalWorkflowExecutionSignaled:
			return historyEvent().getExternalWorkflowExecutionSignaledEventAttributes().getWorkflowExecution().getRunId();

		case WorkflowExecutionStarted:
		case WorkflowExecutionCancelRequested:
		case CompleteWorkflowExecutionFailed:
		case WorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case WorkflowExecutionTimedOut:
		case WorkflowExecutionCanceled:
		case CancelWorkflowExecutionFailed:
		case WorkflowExecutionContinuedAsNew:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionTerminated:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
		case ActivityTaskScheduled:
		case ScheduleActivityTaskFailed:
		case ActivityTaskStarted:
		case ActivityTaskFailed:
		case ActivityTaskTimedOut:
		case ActivityTaskCanceled:
		case ActivityTaskCancelRequested:
		case RequestCancelActivityTaskFailed:
		case RecordMarkerFailed:
		case TimerStarted:
		case StartTimerFailed:
		case CancelTimerFailed:
		case StartChildWorkflowExecutionInitiated:
		case StartChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionTimedOut:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionTerminated:
		case SignalExternalWorkflowExecutionInitiated:
		case SignalExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case RequestCancelExternalWorkflowExecutionFailed:
		case ExternalWorkflowExecutionCancelRequested:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public String reason() {
		switch (type()) {
		case ActivityTaskFailed:
			return historyEvent().getActivityTaskFailedEventAttributes().getReason();
		case ActivityTaskTimedOut:
			return historyEvent().getActivityTaskTimedOutEventAttributes().getTimeoutType();
		case ChildWorkflowExecutionFailed:
			return historyEvent().getChildWorkflowExecutionFailedEventAttributes().getReason();
		case ChildWorkflowExecutionTimedOut:
			return "Child Workflow Execution Timed Out";
		case ChildWorkflowExecutionCanceled:
			return "Child Workflow Execution Canceled";
		case ChildWorkflowExecutionTerminated:
			return "Child Workflow Execution Terminated";
		case SignalExternalWorkflowExecutionFailed:
			return "Signal External Workflow Execution Failed";
		case RequestCancelExternalWorkflowExecutionFailed:
			return "Request Cancel External Workflow Execution Failed";
		case ActivityTaskCanceled:
			return "Activity Task Canceled";
		case WorkflowExecutionCancelRequested:
			return "Workflow Execution Cancel Requested";
		case CompleteWorkflowExecutionFailed:
			return "Complete Workflow Execution Failed";
		case WorkflowExecutionFailed:
			return "Workflow Execution Failed";
		case FailWorkflowExecutionFailed:
			return "Fail Workflow Execution Failed";
		case WorkflowExecutionTimedOut:
			return "Workflow Execution Timed Out";
		case WorkflowExecutionCanceled:
			return "Workflow Execution Canceled";
		case CancelWorkflowExecutionFailed:
			return "Cancel Workflow Execution Failed";
		case ContinueAsNewWorkflowExecutionFailed:
			return "Continue As New Workflow Execution Failed";
		case WorkflowExecutionTerminated:
			return "Workflow Execution Terminated";
		case ScheduleActivityTaskFailed:
			return "Schedule Activity Task Failed";
		case ActivityTaskCancelRequested:
			return "Activity Task Cancel Requested";
		case RequestCancelActivityTaskFailed:
			return "Request Cancel Activity Task Failed";
		case RecordMarkerFailed:
			return "Record Marker Failed";
		case StartTimerFailed:
			return "Start Timer Failed";
		case StartChildWorkflowExecutionFailed:
			return "Start Child Workflow Execution Failed";
		case WorkflowExecutionStarted:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskCompleted:
		case DecisionTaskTimedOut:
		case ActivityTaskScheduled:
		case ActivityTaskStarted:
		case ActivityTaskCompleted:
		case WorkflowExecutionSignaled:
		case MarkerRecorded:
		case TimerStarted:
		case TimerFired:
		case TimerCanceled:
		case CancelTimerFailed:
		case StartChildWorkflowExecutionInitiated:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionCompleted:
		case SignalExternalWorkflowExecutionInitiated:
		case ExternalWorkflowExecutionSignaled:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case ExternalWorkflowExecutionCancelRequested:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case ScheduleLambdaFunctionFailed:
		case StartLambdaFunctionFailed:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public String details() {
		switch (type()) {
		case WorkflowExecutionFailed:
			return historyEvent().getWorkflowExecutionFailedEventAttributes().getDetails();
		case WorkflowExecutionCanceled:
			return historyEvent().getWorkflowExecutionCanceledEventAttributes().getDetails();
		case CompleteWorkflowExecutionFailed:
			return historyEvent().getCompleteWorkflowExecutionFailedEventAttributes().getCause();
		case FailWorkflowExecutionFailed:
			return historyEvent().getFailWorkflowExecutionFailedEventAttributes().getCause();
		case CancelWorkflowExecutionFailed:
			return historyEvent().getCancelWorkflowExecutionFailedEventAttributes().getCause();
		case ContinueAsNewWorkflowExecutionFailed:
			return historyEvent().getContinueAsNewWorkflowExecutionFailedEventAttributes().getCause();
		case WorkflowExecutionCancelRequested:
			return historyEvent().getWorkflowExecutionCancelRequestedEventAttributes().getCause();
		case ScheduleActivityTaskFailed:
			return historyEvent().getScheduleActivityTaskFailedEventAttributes().getCause();
		case RequestCancelActivityTaskFailed:
			return historyEvent().getRequestCancelActivityTaskFailedEventAttributes().getCause();
		case RecordMarkerFailed:
			return historyEvent().getRecordMarkerFailedEventAttributes().getCause();
		case StartTimerFailed:
			return historyEvent().getStartTimerFailedEventAttributes().getCause();
		case StartChildWorkflowExecutionFailed:
			return historyEvent().getStartChildWorkflowExecutionFailedEventAttributes().getCause();
		case WorkflowExecutionTerminated:
			return historyEvent().getWorkflowExecutionTerminatedEventAttributes().getDetails();
		case ActivityTaskFailed:
			return historyEvent().getActivityTaskFailedEventAttributes().getDetails();
		case ActivityTaskTimedOut:
			return historyEvent().getActivityTaskTimedOutEventAttributes().getDetails();
		case ActivityTaskCanceled:
			return historyEvent().getActivityTaskCanceledEventAttributes().getDetails();
		case MarkerRecorded:
			return historyEvent().getMarkerRecordedEventAttributes().getDetails();
		case ChildWorkflowExecutionFailed:
			return historyEvent().getChildWorkflowExecutionFailedEventAttributes().getDetails();
		case ChildWorkflowExecutionTimedOut:
			return historyEvent().getChildWorkflowExecutionTimedOutEventAttributes().getTimeoutType();
		case ChildWorkflowExecutionCanceled:
			return historyEvent().getChildWorkflowExecutionCanceledEventAttributes().getDetails();
		case ChildWorkflowExecutionTerminated:
			return historyEvent().getChildWorkflowExecutionTerminatedEventAttributes().getWorkflowExecution().getRunId();
		case SignalExternalWorkflowExecutionFailed:
			return historyEvent().getSignalExternalWorkflowExecutionFailedEventAttributes().getCause();
		case RequestCancelExternalWorkflowExecutionFailed:
			return historyEvent().getRequestCancelExternalWorkflowExecutionFailedEventAttributes().getCause();
		case WorkflowExecutionStarted:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionTimedOut:
		case WorkflowExecutionContinuedAsNew:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskCompleted:
		case DecisionTaskTimedOut:
		case ActivityTaskScheduled:
		case ActivityTaskStarted:
		case ActivityTaskCompleted:
		case ActivityTaskCancelRequested:
		case WorkflowExecutionSignaled:
		case TimerStarted:
		case TimerFired:
		case TimerCanceled:
		case CancelTimerFailed:
		case StartChildWorkflowExecutionInitiated:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionCompleted:
		case SignalExternalWorkflowExecutionInitiated:
		case ExternalWorkflowExecutionSignaled:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case ExternalWorkflowExecutionCancelRequested:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public Long initialEventId() {
		switch (type()) {
		case DecisionTaskCompleted:
			return historyEvent().getDecisionTaskCompletedEventAttributes().getScheduledEventId();
		case TimerFired:
			return historyEvent().getTimerFiredEventAttributes().getStartedEventId();
		case TimerCanceled:
			return historyEvent().getTimerCanceledEventAttributes().getStartedEventId();
		case StartChildWorkflowExecutionFailed:
			return historyEvent().getStartChildWorkflowExecutionFailedEventAttributes().getInitiatedEventId();
		case ChildWorkflowExecutionStarted:
			return historyEvent().getChildWorkflowExecutionStartedEventAttributes().getInitiatedEventId();
		case ChildWorkflowExecutionCompleted:
			return historyEvent().getChildWorkflowExecutionCompletedEventAttributes().getInitiatedEventId();
		case ChildWorkflowExecutionFailed:
			return historyEvent().getChildWorkflowExecutionFailedEventAttributes().getInitiatedEventId();
		case ChildWorkflowExecutionTimedOut:
			return historyEvent().getChildWorkflowExecutionTimedOutEventAttributes().getInitiatedEventId();
		case ChildWorkflowExecutionCanceled:
			return historyEvent().getChildWorkflowExecutionCanceledEventAttributes().getInitiatedEventId();
		case ChildWorkflowExecutionTerminated:
			return historyEvent().getChildWorkflowExecutionTerminatedEventAttributes().getInitiatedEventId();
		case SignalExternalWorkflowExecutionFailed:
			return historyEvent().getSignalExternalWorkflowExecutionFailedEventAttributes().getInitiatedEventId();
		case ExternalWorkflowExecutionSignaled:
			return historyEvent().getExternalWorkflowExecutionSignaledEventAttributes().getInitiatedEventId();
		case ActivityTaskStarted:
			return historyEvent().getActivityTaskStartedEventAttributes().getScheduledEventId();
		case ActivityTaskCompleted:
			return historyEvent().getActivityTaskCompletedEventAttributes().getScheduledEventId();
		case ActivityTaskFailed:
			return historyEvent().getActivityTaskFailedEventAttributes().getScheduledEventId();
		case ActivityTaskTimedOut:
			return historyEvent().getActivityTaskTimedOutEventAttributes().getScheduledEventId();
		case ActivityTaskCanceled:
			return historyEvent().getActivityTaskCanceledEventAttributes().getScheduledEventId();
		case StartTimerFailed:
		case WorkflowExecutionStarted:
		case WorkflowExecutionCancelRequested:
		case WorkflowExecutionCompleted:
		case CompleteWorkflowExecutionFailed:
		case WorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case WorkflowExecutionTimedOut:
		case WorkflowExecutionCanceled:
		case CancelWorkflowExecutionFailed:
		case WorkflowExecutionContinuedAsNew:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionTerminated:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
		case WorkflowExecutionSignaled:
		case MarkerRecorded:
		case RecordMarkerFailed:
		case TimerStarted:
		case CancelTimerFailed:
		case StartChildWorkflowExecutionInitiated:
		case SignalExternalWorkflowExecutionInitiated:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case RequestCancelExternalWorkflowExecutionFailed:
		case ExternalWorkflowExecutionCancelRequested:
			// activity tasks
		case ActivityTaskScheduled:
		case ScheduleActivityTaskFailed:
		case ActivityTaskCancelRequested:
		case RequestCancelActivityTaskFailed:
			return historyEvent().getEventId();

		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	/**
	 * @return the initial event or itself
	 */
	private WorkflowEvent initialEvent() {
		Long initialEventId = initialEventId();
		return initialEventId == id() ? this : history.getEventById(initialEventId);
	}

	public String cause() {
		switch (type()) {
		case CompleteWorkflowExecutionFailed:
			return historyEvent().getCompleteWorkflowExecutionFailedEventAttributes().getCause();
		case FailWorkflowExecutionFailed:
			return historyEvent().getFailWorkflowExecutionFailedEventAttributes().getCause();
		case CancelWorkflowExecutionFailed:
			return historyEvent().getCancelWorkflowExecutionFailedEventAttributes().getCause();
		case ContinueAsNewWorkflowExecutionFailed:
			return historyEvent().getContinueAsNewWorkflowExecutionFailedEventAttributes().getCause();
		case WorkflowExecutionCancelRequested:
			return historyEvent().getWorkflowExecutionCancelRequestedEventAttributes().getCause();
		case ScheduleActivityTaskFailed:
			return historyEvent().getScheduleActivityTaskFailedEventAttributes().getCause();
		case RequestCancelActivityTaskFailed:
			return historyEvent().getRequestCancelActivityTaskFailedEventAttributes().getCause();
		case RecordMarkerFailed:
			return historyEvent().getRecordMarkerFailedEventAttributes().getCause();
		case StartTimerFailed:
			return historyEvent().getStartTimerFailedEventAttributes().getCause();
		case StartChildWorkflowExecutionFailed:
			return historyEvent().getStartChildWorkflowExecutionFailedEventAttributes().getCause();
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskScheduled:
		case ActivityTaskStarted:
		case ActivityTaskTimedOut:
		case CancelTimerFailed:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
		case ExternalWorkflowExecutionCancelRequested:
		case ExternalWorkflowExecutionSignaled:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case MarkerRecorded:
		case RequestCancelExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case ScheduleLambdaFunctionFailed:
		case SignalExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionInitiated:
		case StartChildWorkflowExecutionInitiated:
		case StartLambdaFunctionFailed:
		case TimerCanceled:
		case TimerFired:
		case TimerStarted:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionSignaled:
		case WorkflowExecutionStarted:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public TaskType taskType() {
		switch (type()) {
		case ScheduleActivityTaskFailed:
			return toTaskType(historyEvent().getScheduleActivityTaskFailedEventAttributes().getActivityType());
		case ActivityTaskScheduled:
			return toTaskType(historyEvent().getActivityTaskScheduledEventAttributes().getActivityType());
		case ActivityTaskTimedOut:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskStarted:
		case ActivityTaskCanceled:
			return initialEvent().taskType();
			// cannot
		case RequestCancelActivityTaskFailed:
		case ActivityTaskCancelRequested:
		case CompleteWorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case CancelWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionCancelRequested:
		case RecordMarkerFailed:
		case StartTimerFailed:
		case StartChildWorkflowExecutionFailed:
		case CancelTimerFailed:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
		case ExternalWorkflowExecutionCancelRequested:
		case ExternalWorkflowExecutionSignaled:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case MarkerRecorded:
		case RequestCancelExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case ScheduleLambdaFunctionFailed:
		case SignalExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionInitiated:
		case StartChildWorkflowExecutionInitiated:
		case StartLambdaFunctionFailed:
		case TimerCanceled:
		case TimerFired:
		case TimerStarted:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionSignaled:
		case WorkflowExecutionStarted:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	private TaskType toTaskType(final ActivityType activityType) {
		return new TaskType(activityType.getName(), activityType.getVersion());
	}

	public String timerId() {
		switch (type()) {
		case StartTimerFailed:
			return historyEvent().getStartTimerFailedEventAttributes().getTimerId();
		case CancelTimerFailed:
			return historyEvent().getCancelTimerFailedEventAttributes().getTimerId();
		case TimerCanceled:
			return historyEvent().getTimerCanceledEventAttributes().getTimerId();
		case TimerFired:
			return historyEvent().getTimerFiredEventAttributes().getTimerId();
		case TimerStarted:
			return historyEvent().getTimerStartedEventAttributes().getTimerId();
		case ScheduleActivityTaskFailed:
		case ActivityTaskScheduled:
		case CompleteWorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case CancelWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionCancelRequested:
		case RequestCancelActivityTaskFailed:
		case RecordMarkerFailed:
		case StartChildWorkflowExecutionFailed:
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskStarted:
		case ActivityTaskTimedOut:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
		case ExternalWorkflowExecutionCancelRequested:
		case ExternalWorkflowExecutionSignaled:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case MarkerRecorded:
		case RequestCancelExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case ScheduleLambdaFunctionFailed:
		case SignalExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionInitiated:
		case StartChildWorkflowExecutionInitiated:
		case StartLambdaFunctionFailed:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionSignaled:
		case WorkflowExecutionStarted:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public String markerName() {
		switch (type()) {
		case MarkerRecorded:
			return historyEvent().getMarkerRecordedEventAttributes().getMarkerName();
		case RecordMarkerFailed:
			return historyEvent().getRecordMarkerFailedEventAttributes().getMarkerName();
		case ScheduleActivityTaskFailed:
		case ActivityTaskScheduled:
		case CompleteWorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case CancelWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionCancelRequested:
		case RequestCancelActivityTaskFailed:
		case StartTimerFailed:
		case StartChildWorkflowExecutionFailed:
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskStarted:
		case ActivityTaskTimedOut:
		case CancelTimerFailed:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
		case ExternalWorkflowExecutionCancelRequested:
		case ExternalWorkflowExecutionSignaled:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case RequestCancelExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case ScheduleLambdaFunctionFailed:
		case SignalExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionInitiated:
		case StartChildWorkflowExecutionInitiated:
		case StartLambdaFunctionFailed:
		case TimerCanceled:
		case TimerFired:
		case TimerStarted:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionSignaled:
		case WorkflowExecutionStarted:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	public String taskId() {
		switch (type()) {
		case ScheduleActivityTaskFailed:
			return historyEvent().getScheduleActivityTaskFailedEventAttributes().getActivityId();
		case ActivityTaskScheduled:
			return historyEvent().getActivityTaskScheduledEventAttributes().getActivityId();
		case CompleteWorkflowExecutionFailed:
		case FailWorkflowExecutionFailed:
		case CancelWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case WorkflowExecutionCancelRequested:
		case RequestCancelActivityTaskFailed:
		case RecordMarkerFailed:
		case StartTimerFailed:
		case StartChildWorkflowExecutionFailed:
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case ActivityTaskCompleted:
		case ActivityTaskFailed:
		case ActivityTaskStarted:
		case ActivityTaskTimedOut:
		case CancelTimerFailed:
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:
		case ExternalWorkflowExecutionCancelRequested:
		case ExternalWorkflowExecutionSignaled:
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case MarkerRecorded:
		case RequestCancelExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case ScheduleLambdaFunctionFailed:
		case SignalExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionInitiated:
		case StartChildWorkflowExecutionInitiated:
		case StartLambdaFunctionFailed:
		case TimerCanceled:
		case TimerFired:
		case TimerStarted:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionSignaled:
		case WorkflowExecutionStarted:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
			return null;
		default:
			throw new IllegalArgumentException("Unknown EventType " + type());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1000);
		sb.append(String.format("%s: %s, %s, ", type(), id(), initialEventId()));
		appendIf(signalName(), sb);
		appendIf(markerName(), sb);
		appendIf(timerId(), sb);
		appendIf(taskType(), sb);
		appendIf(taskId(), sb);
		appendIf(control(), sb);
		appendIf(cause(), sb);
		appendIf(reason(), sb);
		appendIf(details(), sb);
		appendIf(input(), sb);
		appendIf(output(), sb);
		sb.append(" ");
		sb.append(eventTimestamp());
		return sb.toString();
	}

	private static void appendIf(final Object value, final StringBuilder sb) {
		if (value != null) {
			sb.append(" ");
			sb.append(value);
			sb.append(",");
		}
	}

	/**
	 * Sort by eventId descending (most recent event first).
	 */
	@Override
	public int compareTo(final WorkflowEvent event) {
		return event.id().compareTo(id());
	}

}
