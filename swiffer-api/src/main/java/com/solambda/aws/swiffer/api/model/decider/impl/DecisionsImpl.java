package com.solambda.aws.swiffer.api.model.decider.impl;

import java.time.Duration;
import java.util.*;

import com.amazonaws.services.simpleworkflow.model.*;
import com.solambda.aws.swiffer.api.model.TaskType;
import com.solambda.aws.swiffer.api.model.decider.Decisions;
import com.solambda.aws.swiffer.api.model.tasks.TaskOptions;

public class DecisionsImpl implements Decisions {

	private List<Decision> decisions;

	public DecisionsImpl() {
		decisions = new ArrayList<Decision>();
	}

	@Override
	public Collection<Decision> get() {
		return Collections.unmodifiableList(decisions);
	}

	private Decision newDecision(final DecisionType decisionType) {
		Decision decision = new Decision().withDecisionType(decisionType);
		decisions.add(decision);
		return decision;
	}

	@Override
	public void completeWorfklow() {
		completeWorfklow(null);
	}

	@Override
	public void completeWorfklow(final String result) {
		newDecision(DecisionType.CompleteWorkflowExecution)
				.withCompleteWorkflowExecutionDecisionAttributes(new CompleteWorkflowExecutionDecisionAttributes()
						.withResult(result));
	}

	@Override
	public void cancelWorfklow() {
		cancelWorfklow(null);
	}

	@Override
	public void cancelWorfklow(final String details) {
		newDecision(DecisionType.CancelWorkflowExecution)
				.withCancelWorkflowExecutionDecisionAttributes(new CancelWorkflowExecutionDecisionAttributes()
						.withDetails(details));
	}

	@Override
	public void failWorfklow() {
		failWorfklow(null);
	}

	@Override
	public void failWorfklow(final String reason) {
		failWorfklow(null, null);
	}

	@Override
	public void failWorfklow(final String reason, final String details) {
		newDecision(DecisionType.FailWorkflowExecution)
				.withFailWorkflowExecutionDecisionAttributes(new FailWorkflowExecutionDecisionAttributes()
						.withReason(details)
						.withDetails(details));
	}

	@Override
	public void scheduleTask(final TaskType type) {
		scheduleTask(type, null, new TaskOptions());
	}

	@Override
	public void scheduleTask(final TaskType type, final String input) {
		scheduleTask(type, input, new TaskOptions());
	}

	@Override
	public void scheduleTask(final TaskType type, final TaskOptions options) {
		scheduleTask(type, null, options == null ? new TaskOptions() : options);
	}

	@Override
	public void scheduleTask(final TaskType type, final String input, final TaskOptions options) {
		newDecision(DecisionType.ScheduleActivityTask)
				.withScheduleActivityTaskDecisionAttributes(new ScheduleActivityTaskDecisionAttributes()
						.withActivityType(new ActivityType().withName(type.name()).withVersion(type.version()))
						.withActivityId(UUID.randomUUID().toString())
						.withControl(options.control())
						.withHeartbeatTimeout(options.heartbeatTimeout())
						.withInput(input)
						.withScheduleToCloseTimeout(options.scheduleToCloseTimeout())
						.withScheduleToStartTimeout(options.scheduleToStartTimeout())
						.withStartToCloseTimeout(options.startToCloseTimeout())
						.withTaskList(options.taskList())
						.withTaskPriority(options.taskPriority()));
	}

	@Override
	public void startTimer(final String timerId, final Duration duration) {
		startTimer(timerId, null, duration);
	}

	@Override
	public void startTimer(final String timerId, final String control, final Duration duration) {
		newDecision(DecisionType.StartTimer)
				.withStartTimerDecisionAttributes(new StartTimerDecisionAttributes()
						.withTimerId(timerId)
						.withStartToFireTimeout(Long.toString(duration.getSeconds()))
						.withControl(control)
				);
	}

	@Override
	public void cancelTimer(final String timerId) {
		cancelTimer(timerId, false);
	}

	@Override
	public void cancelTimer(final String timerId, final boolean forceDecisionMaking) {
		// TODO: we should add a force decision
		newDecision(DecisionType.CancelTimer)
				.withCancelTimerDecisionAttributes(new CancelTimerDecisionAttributes()
						.withTimerId(timerId));
		if (forceDecisionMaking) {
			forceDecisionMaking();
		}
	}

	private void forceDecisionMaking() {
		// we publish a fake timer
		startTimer(FORCE_TIMER_ID, "fake timer to trigger a decision task", Duration.ZERO);
	}

	@Override
	public void createMarker(final String markerName) {
		createMarker(markerName, null);
	}

	@Override
	public void createMarker(final String markerName, final String details) {
		newDecision(DecisionType.RecordMarker)
				.withRecordMarkerDecisionAttributes(new RecordMarkerDecisionAttributes()
						.withMarkerName(markerName)
						.withDetails(details)
				);
	}

}
