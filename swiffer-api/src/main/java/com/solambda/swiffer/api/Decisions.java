package com.solambda.swiffer.api;

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
}
