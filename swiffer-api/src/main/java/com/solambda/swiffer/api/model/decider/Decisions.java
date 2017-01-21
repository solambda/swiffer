package com.solambda.swiffer.api.model.decider;

import java.time.Duration;
import java.util.Collection;

import com.amazonaws.services.simpleworkflow.model.Decision;
import com.solambda.swiffer.api.model.TaskType;
import com.solambda.swiffer.api.model.tasks.TaskOptions;

/**
 * Provide method for creating decisions.
 * <p>
 */
public interface Decisions {

	public static final String FORCE_TIMER_ID = "___FORCE_DECISION_MAKING_TIMER___";

	/**
	 * @return a copy of the decisions made
	 */
	public Collection<Decision> get();

	// WF EXECUTIONS
	public void completeWorfklow();

	public void completeWorfklow(String result);

	public void cancelWorfklow();

	public void cancelWorfklow(String details);

	public void failWorfklow();

	public void failWorfklow(String reason);

	public void failWorfklow(String reason, String details);

	// TASKS

	public void scheduleTask(TaskType taskType);

	public void scheduleTask(TaskType taskType, String input);

	// public <I> void scheduleTask(TaskType taskType, I input);
	// public <I> void scheduleTask(TaskType taskType, I input, Mapper<I>
	// mapper);
	public void scheduleTask(TaskType taskType, TaskOptions options);

	public void scheduleTask(TaskType taskType, String input, TaskOptions options);

	// public <I> void scheduleTask(TaskType taskType, I input, TaskOptions
	// options);
	// public <I> void scheduleTask(TaskType taskType, I input, Mapper<I>
	// mapper, TaskOptions options);

	// TIMERS

	public void startTimer(String timerId, Duration duration);

	public void startTimer(String timerId, String control, Duration duration);

	/**
	 * Cancel the started timer.
	 * <p>
	 * Important: the decider will not be notified unless another decision is
	 * taken. See {@link #cancelTimer(String, boolean)} to force decision
	 * making.
	 * <p>
	 *
	 * @param timerId
	 *            the timer to cancel
	 */
	public void cancelTimer(String timerId);

	/**
	 * Cancel the started timer, forcing or not a decision to be taken.
	 * <p>
	 * if force is false, cancelling the timer will not trigger a new decision
	 * task.
	 *
	 *
	 * @param timerName
	 * @param forceDecisionMaking
	 */
	public void cancelTimer(String timerName, boolean forceDecisionMaking);

	// MARKERS

	public void createMarker(String markerName);

	public void createMarker(String markerName, String details);

}
