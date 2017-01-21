package com.solambda.aws.swiffer.api.model.decider;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.aws.swiffer.api.model.decider.context.EventContextImpl;
import com.solambda.aws.swiffer.api.model.decider.handler.*;
import com.google.common.base.Objects;

public class EventDelegatorDecider implements Decider {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventDelegatorDecider.class);
	private EventContextHandlerRegistry registry;

	public EventDelegatorDecider(final EventContextHandlerRegistry registry) {
		super();
		this.registry = registry;
	}

	@Override
	public void makeDecisions(final DecisionContext context, final Decisions decideTo) {
		List<WorkflowEvent> events = context.newEvents();
		LOGGER.debug("Processing {} new events", events.size());
		for (WorkflowEvent event : events) {
			LOGGER.debug("{}", event);
			EventContextImpl eventContext = new EventContextImpl(context, event);
			EventContextHandler<?> handler = registry.get(eventContext);
			if (handler != null) {
				callHandler(event.type(), eventContext, handler, decideTo);
			} else {
				callDefault(event.type(), eventContext, decideTo);
			}
		}
		LOGGER.debug("{} new decisions made", decideTo.get().size());

	}

	private void callHandler(
			final EventType type,
			final EventContextImpl impl,
			final EventContextHandler<?> delegate,
			final Decisions decisions) {
		switch (type) {
		case WorkflowExecutionStarted:
			((WorkflowStartedHandler) delegate).onWorkflowStarted(impl, decisions);
			break;
		case WorkflowExecutionSignaled:
			((SignalReceivedHandler) delegate).onSignalReceived(impl, decisions);
			break;
		case WorkflowExecutionCancelRequested:
			((WorkflowCancelRequestedHandler) delegate).onWorkflowCancelRequested(impl, decisions);
			break;
		// ACTIVITY TASKS
		case ScheduleActivityTaskFailed:
			((TaskScheduleFailedHandler) delegate).onTaskScheduleFailed(impl, decisions);
			break;
		case ActivityTaskCompleted:
			((TaskCompletedHandler) delegate).onTaskCompleted(impl, decisions);
			break;
		case ActivityTaskFailed:
			((TaskFailedHandler) delegate).onTaskFailed(impl, decisions);
			break;
		case ActivityTaskTimedOut:
			((TaskTimedOutHandler) delegate).onTaskTimedOut(impl, decisions);
			break;

		// TIMERS
		case TimerCanceled:
			((TimerCanceledHandler) delegate).onTimerCanceled(impl, decisions);
			break;
		case TimerFired:
			if (!Objects.equal(Decisions.FORCE_TIMER_ID, impl.timerId())) {
				((TimerFiredHandler) delegate).onTimerFired(impl, decisions);
			}
			break;

		// MARKERS
		case MarkerRecorded:
			((MarkerRecordedHandler) delegate).onMarkerRecorded(impl, decisions);
			break;

		// P2 WORKFLOW EXECUTION
		case FailWorkflowExecutionFailed:
		case CompleteWorkflowExecutionFailed:
		case ContinueAsNewWorkflowExecutionFailed:
		case CancelWorkflowExecutionFailed:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
			// P2 TIMER
		case TimerStarted:
		case StartTimerFailed:
		case CancelTimerFailed:
			// P2 MARKER
		case RecordMarkerFailed:
			// P2 ACTIVITY TASKS
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case RequestCancelActivityTaskFailed:

			// P3 ACTIVITY TASKS
		case ActivityTaskScheduled:
		case ActivityTaskStarted:

			// P2 CHILD WORKFLOW
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case StartChildWorkflowExecutionFailed:
		case StartChildWorkflowExecutionInitiated:

			// P2 DECISION TASKS
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
		case DecisionTaskTimedOut:

			// P2 EXTERNAL WORKFLOW EXECUTION
		case ExternalWorkflowExecutionCancelRequested:
		case ExternalWorkflowExecutionSignaled:
		case RequestCancelExternalWorkflowExecutionFailed:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case SignalExternalWorkflowExecutionFailed:
		case SignalExternalWorkflowExecutionInitiated:

			// P2 LAMBDA
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
		case ScheduleLambdaFunctionFailed:
		case StartLambdaFunctionFailed:

			//
		default:
			break;
		}
	}

	private void callDefault(final EventType type, final EventContextImpl eventContext, final Decisions decideTo) {
		switch (type) {
		// nothing notable must be done
		case WorkflowExecutionSignaled:
			// really ?
			decideTo.failWorfklow("a signal is received and nothing is done !", eventContext.toString());
			break;
		case ScheduleActivityTaskFailed:
			decideTo.failWorfklow("impossible to schedule the task !", eventContext.toString());
			break;
		case ActivityTaskFailed:
			// retry ?
			decideTo.failWorfklow("a task execution failed !", eventContext.toString());
			break;
		case ActivityTaskTimedOut:
			// retry?
			decideTo.failWorfklow("a task execution failed with timeout!", eventContext.toString());
			break;
		case RequestCancelActivityTaskFailed:
			decideTo.failWorfklow("impossible to request cancel an activity task!", eventContext.toString());
			break;
		case CompleteWorkflowExecutionFailed:
			decideTo.failWorfklow("impossible to complete the workflow!", eventContext.toString());
			break;
		case ContinueAsNewWorkflowExecutionFailed:
			decideTo.failWorfklow("impossible to continue as new workflow!", eventContext.toString());
			break;
		case CancelWorkflowExecutionFailed:
			decideTo.failWorfklow("impossible to cancel the workflow!", eventContext.toString());
			break;
		case StartTimerFailed:
			decideTo.failWorfklow("impossible to start a timer!", eventContext.toString());
			break;
		case CancelTimerFailed:
			decideTo.failWorfklow("impossible to cancel a timer!", eventContext.toString());
			break;
		case RecordMarkerFailed:
			decideTo.failWorfklow("impossible to record a marker!", eventContext.toString());
			break;
		case DecisionTaskTimedOut:
			decideTo.failWorfklow("too long for taking decision !", eventContext.toString());
			break;
		case ScheduleLambdaFunctionFailed:
			decideTo.failWorfklow("impossible to schedule lambdra function!", eventContext.toString());
			break;
		case StartLambdaFunctionFailed:
			decideTo.failWorfklow("impossible to start lambda function!", eventContext.toString());
			break;
		case RequestCancelExternalWorkflowExecutionFailed:
			decideTo.failWorfklow("impossible to request cancel external workflow!", eventContext.toString());
			break;
		case SignalExternalWorkflowExecutionFailed:
			decideTo.failWorfklow("impossible to signal external workflow!", eventContext.toString());
			break;
		case StartChildWorkflowExecutionFailed:
			decideTo.failWorfklow("impossible to start child workflow!", eventContext.toString());
			break;
		// =========== IGNORE ALL THOSE NOTHANDLED EVENTS ==============

		// P2 TIMER
		case TimerStarted:
		case TimerCanceled:
		case TimerFired:
			// P2 MARKER
		case MarkerRecorded:
			// P2 WORKFLOW EXECUTION
		case WorkflowExecutionStarted:
		case WorkflowExecutionCancelRequested:
		case FailWorkflowExecutionFailed:
		case WorkflowExecutionCanceled:
		case WorkflowExecutionCompleted:
		case WorkflowExecutionContinuedAsNew:
		case WorkflowExecutionFailed:
		case WorkflowExecutionTerminated:
		case WorkflowExecutionTimedOut:
			// P3 ACTIVITY TASKS
		case ActivityTaskCompleted:
		case ActivityTaskCancelRequested:
		case ActivityTaskCanceled:
		case ActivityTaskScheduled:
		case ActivityTaskStarted:
			// P2 DECISION TASKS
		case DecisionTaskCompleted:
		case DecisionTaskScheduled:
		case DecisionTaskStarted:
			// P2 LAMBDA
		case LambdaFunctionCompleted:
		case LambdaFunctionFailed:
		case LambdaFunctionScheduled:
		case LambdaFunctionStarted:
		case LambdaFunctionTimedOut:
			// P2 EXTERNAL WORKFLOW EXECUTION
		case ExternalWorkflowExecutionCancelRequested:
		case ExternalWorkflowExecutionSignaled:
		case RequestCancelExternalWorkflowExecutionInitiated:
		case SignalExternalWorkflowExecutionInitiated:
			// P2 CHILD WORKFLOW
		case ChildWorkflowExecutionCanceled:
		case ChildWorkflowExecutionCompleted:
		case ChildWorkflowExecutionFailed:
		case ChildWorkflowExecutionStarted:
		case ChildWorkflowExecutionTerminated:
		case ChildWorkflowExecutionTimedOut:
		case StartChildWorkflowExecutionInitiated:
		default:
			break;
		}
	}

}
