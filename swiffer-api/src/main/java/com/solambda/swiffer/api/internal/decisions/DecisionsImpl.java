package com.solambda.swiffer.api.internal.decisions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.mapper.DataMapper;

public class DecisionsImpl implements Decisions {
	private static final Logger LOGGER = LoggerFactory.getLogger(DecisionsImpl.class);

	private List<Decision> decisions;
	private final DataMapper dataMapper;
	private final DurationTransformer durationTransformer;

	public DecisionsImpl(DataMapper dataMapper, DurationTransformer durationTransformer) {
		this.decisions = new ArrayList<>();
		this.dataMapper = dataMapper;
		this.durationTransformer = durationTransformer;
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
		return dataMapper.serialize(object);
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
					.withTaskList(options.taskList())
					.withTaskPriority(nullSafeToString(options.taskPriority()))
					.withHeartbeatTimeout(getActivityTimeout(options.getMaxHeartbeatDuration()))
					.withScheduleToCloseTimeout(getActivityTimeout(options.getScheduleToCloseDuration()))
					.withScheduleToStartTimeout(getActivityTimeout(options.getScheduleToStartDuration()))
					.withStartToCloseTimeout(getActivityTimeout(options.getStartToCloseDuration()));
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
	@Override
	public Decisions failWorkflow(final String reason, final String details) {
		newDecision(DecisionType.FailWorkflowExecution)
				.withFailWorkflowExecutionDecisionAttributes(new FailWorkflowExecutionDecisionAttributes()
						.withReason(reason)
						.withDetails(details));
		return this;
	}

	private String nullSafeToString(final Object object) {
		return object == null ? null : object.toString();
	}

	@Override
	public Decisions startTimer(final String timerId, final Duration duration) {
		return startTimer(timerId, duration, null);
	}

	@Override
	public Decisions startTimer(final String timerId, final Duration duration, final Object control) {

		newDecision(DecisionType.StartTimer)
				.withStartTimerDecisionAttributes(new StartTimerDecisionAttributes()
						.withTimerId(timerId)
						.withStartToFireTimeout(getTimerDuration(timerId, duration))
						.withControl(serialize(control)));
		return this;
	}

	@Override
	public Decisions cancelTimer(final String timerId) {
		newDecision(DecisionType.CancelTimer)
				.withCancelTimerDecisionAttributes(new CancelTimerDecisionAttributes()
						.withTimerId(timerId));
		return this;
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

	private String getTimerDuration(String timerId, Duration duration) {
		Duration transformed;
		if (duration == null) {
			LOGGER.warn("Required duration for timer {} was null, use ZERO duration instead.", timerId);
			transformed = durationTransformer.transform(Duration.ZERO);
		} else {
			transformed = durationTransformer.transform(duration);
		}

		return Long.toString(transformed.getSeconds());
	}

	private String getActivityTimeout(Duration duration) {
		if (duration == null) {
			return "NONE";
		}
		Duration transformed = durationTransformer.transform(duration);
		return Long.toString(transformed.getSeconds());
	}
}
