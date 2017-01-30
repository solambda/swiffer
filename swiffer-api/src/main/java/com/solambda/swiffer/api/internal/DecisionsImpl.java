package com.solambda.swiffer.api.internal;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.simpleworkflow.model.ActivityType;
import com.amazonaws.services.simpleworkflow.model.CancelTimerDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.CancelWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.RecordMarkerDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.ScheduleActivityTaskDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.StartTimerDecisionAttributes;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.ActivityOptions;
import com.solambda.swiffer.api.Decisions;

public class DecisionsImpl implements Decisions {

	private List<Decision> decisions;

	public DecisionsImpl() {
		this.decisions = new ArrayList<Decision>();
	}

	/**
	 * @return the list of decisions
	 */
	public Collection<Decision> get() {
		return Collections.unmodifiableList(this.decisions);
	}

	/**
	 * add a new decision to the list of deicisions for the given type, and
	 * return the decision to allow configuring it
	 *
	 * @param decisionType
	 * @return
	 */
	private Decision newDecision(final DecisionType decisionType) {
		final Decision decision = new Decision().withDecisionType(decisionType);
		this.decisions.add(decision);
		return decision;
	}

	/**
	 * @param object
	 * @return
	 */
	private String serialize(final Object object) {
		// TODO: user jackson serialization
		// TODO: allow customization of the serializer
		return object == null ? null : object.toString();
	}

	@Override
	public Decisions scheduleActivityTask(final Class<?> activityType, final Object input) {
		return doScheduleActivityTask(activityType, null, input, null);
	}

	@Override
	public Decisions scheduleActivityTask(final Class<?> activityType, final Object input, final String activityId,
			final ActivityOptions options) {
		return doScheduleActivityTask(activityType, activityId, input, options);
	}

	@Override
	public Decisions scheduleActivityTask(final Class<?> activityTypeClass, final Object input,
			final ActivityOptions options) {
		return doScheduleActivityTask(activityTypeClass, null, input, options);
	}

	@Override
	public Decisions scheduleActivityTask(final Class<?> activityType, final ActivityOptions options) {
		return doScheduleActivityTask(activityType, null, null, options);
	}

	private Decisions doScheduleActivityTask(final Class<?> activityTypeClass, final String activityId,
			final Object input,
			final ActivityOptions options) {
		ScheduleActivityTaskDecisionAttributes attributes = new ScheduleActivityTaskDecisionAttributes()
				.withActivityType(toActivityType(activityTypeClass))
				.withActivityId(activityId == null ? UUID.randomUUID().toString() : activityId)
				.withInput(serialize(input));
		if (options != null) {
			attributes = attributes
					.withControl(options.control())
					.withHeartbeatTimeout(options.heartbeatTimeout())
					.withScheduleToCloseTimeout(options.scheduleToCloseTimeout())
					.withScheduleToStartTimeout(options.scheduleToStartTimeout())
					.withStartToCloseTimeout(options.startToCloseTimeout())
					.withTaskList(options.taskList())
					.withTaskPriority(nullSafeToString(options.taskPriority()));
		}
		newDecision(DecisionType.ScheduleActivityTask)
				.withScheduleActivityTaskDecisionAttributes(attributes);
		return this;
	}

	private ActivityType toActivityType(final Class<?> activityTypeClass) {
		Preconditions.checkArgument(activityTypeClass.isInterface());
		final com.solambda.swiffer.api.ActivityType activityType = activityTypeClass
				.getAnnotation(com.solambda.swiffer.api.ActivityType.class);
		Preconditions.checkState(activityType != null, "The interface %s, should be annotated with %s!",
				activityTypeClass, ActivityType.class);
		return new ActivityType().withName(activityType.name()).withVersion(activityType.version());
	}

	@Override
	public Decisions completeWorfklow() {
		return completeWorfklow(null);
	}

	@Override
	public Decisions completeWorfklow(final Object result) {
		newDecision(DecisionType.CompleteWorkflowExecution)
				.withCompleteWorkflowExecutionDecisionAttributes(
						new CompleteWorkflowExecutionDecisionAttributes()
								.withResult(serialize(result)));
		return this;
	}

	// TODO
	private void cancelWorfklow(final String details) {
		newDecision(DecisionType.CancelWorkflowExecution)
				.withCancelWorkflowExecutionDecisionAttributes(new CancelWorkflowExecutionDecisionAttributes()
						.withDetails(details));
	}

	// TODO
	private Decisions failWorfklow(final String reason, final String details) {
		newDecision(DecisionType.FailWorkflowExecution)
				.withFailWorkflowExecutionDecisionAttributes(new FailWorkflowExecutionDecisionAttributes()
						.withReason(details)
						.withDetails(details));
		return this;
	}

	private String nullSafeToString(final Object object) {
		return object == null ? null : object.toString();
	}

	// TODO
	private void startTimer(final String timerId, final String control, final Duration duration) {
		newDecision(DecisionType.StartTimer)
				.withStartTimerDecisionAttributes(new StartTimerDecisionAttributes()
						.withTimerId(timerId)
						.withStartToFireTimeout(Long.toString(duration.getSeconds()))
						.withControl(control));
	}

	// TODO
	private void cancelTimer(final String timerId) {
		// TODO: we should add a force decision
		newDecision(DecisionType.CancelTimer)
				.withCancelTimerDecisionAttributes(new CancelTimerDecisionAttributes()
						.withTimerId(timerId));
	}

	// TODO
	public void createMarker(final String markerName, final String details) {
		newDecision(DecisionType.RecordMarker)
				.withRecordMarkerDecisionAttributes(new RecordMarkerDecisionAttributes()
						.withMarkerName(markerName)
						.withDetails(details));
	}

	@Override
	public String toString() {
		return "Decisions=" + this.decisions + "";
	}

}
