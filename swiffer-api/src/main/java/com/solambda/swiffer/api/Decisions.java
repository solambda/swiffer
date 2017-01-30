package com.solambda.swiffer.api;

import java.time.Duration;

/**
 * A convenient builder of decisions.
 * <p>
 * It must be passed as a parameter of any event handler method.
 * <p>
 */
@EventHandlerCommonParameter
public interface Decisions {
	/**
	 * Add a "schedule activity task" decision.
	 * <p>
	 * The activity id will be computed by concatenating the activity name, the
	 * version and a timestamp.
	 *
	 * @param activityType
	 *            class annotated with {@link ActivityType}
	 * @param input
	 *            the input to provide to the activity task
	 * @return this decision object
	 */
	Decisions scheduleActivityTask(Class<?> activityType, Object input);

	/**
	 * Add a "schedule activity task" decision. *
	 * <p>
	 * The activity id will be computed by concatenating the activity name, the
	 * version and a timestamp.
	 *
	 * @param activityType
	 *            class annotated with {@link ActivityType}
	 * @param options
	 *            the {@link ActivityOptions}
	 * @return this decision object
	 */
	Decisions scheduleActivityTask(Class<?> activityType, ActivityOptions options);

	/**
	 * Add a "schedule activity task" decision. *
	 * <p>
	 * The activity id will be computed by concatenating the activity name, the
	 * version and a timestamp.
	 *
	 * @param activityType
	 *            class annotated with {@link ActivityType}
	 * @param input
	 *            the input to provide to the activity task
	 * @param options
	 *            the {@link ActivityOptions}
	 * @return this decision object
	 */
	Decisions scheduleActivityTask(Class<?> activityType, Object input, ActivityOptions options);

	/**
	 * Add a "schedule activity task" decision.
	 * <p>
	 *
	 * @param activityType
	 *            class annotated with {@link ActivityType}
	 * @param input
	 *            the input to provide to the activity task
	 * @param activityId
	 *            the id to give to this activity task, in order to cancel it
	 *            later
	 * @param options
	 *            the {@link ActivityOptions}
	 * @return this decision object
	 */
	Decisions scheduleActivityTask(final Class<?> activityType, Object input, String activityId,
			ActivityOptions options);

	/**
	 * Add a "complete workflow execution" decision.
	 *
	 * @return this decision object
	 */
	Decisions completeWorfklow();

	/**
	 * Add a "complete workflow execution" decision.
	 *
	 * @param result
	 *            The result of the workflow execution
	 * @return this decision object
	 */
	Decisions completeWorfklow(Object result);

	/**
	 * Add a "start timer" decision.
	 *
	 * @param timerId
	 *            the id of the timer, must not be equals to an existing started
	 *            timer that is not fired or cancelled
	 * @param duration
	 *            duration of the timer before firing
	 * @return this decisions object
	 */
	Decisions startTimer(String timerId, Duration duration);

	/**
	 * Add a "start timer" decision.
	 *
	 * @param timerId
	 *            the id of the timer, must not be equals to an existing started
	 *            timer that is not fired or cancelled
	 * @param duration
	 *            duration of the timer before firing
	 * @param control
	 *            an optional object to be set in the timer attributes
	 * @return this decisions object
	 */
	Decisions startTimer(String timerId, Duration duration, Object control);
}
