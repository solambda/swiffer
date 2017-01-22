package com.solambda.swiffer.api;

import java.time.Duration;

import com.amazonaws.services.simpleworkflow.model.TaskList;

/**
 * Options to be used when making a {@link Decisions#}.
 * <p>
 * TODO: consider renaming the setter as defined in the AWS SWF documentation
 */
public class ActivityOptions {

	private String control;

	private String taskList;

	// MIN < 0 < MAX (max is higher priority)
	private Integer taskPriority;

	// NONE allowed
	private String scheduleToCloseTimeout;

	// NONE allowed
	private String scheduleToStartTimeout;

	// NONE allowed
	private String startToCloseTimeout;

	// NONE allowed
	private String heartbeatTimeout;

	public ActivityOptions() {
	}

	/**
	 * Optional. Data attached to the event that can be used by the decider in
	 * subsequent workflow tasks. This data is not sent to the activity.
	 *
	 * @param control
	 * @return
	 */
	public ActivityOptions control(final String control) {
		this.control = control;
		return this;
	}

	/**
	 * If set, specifies the name of the task list in which to schedule the
	 * activity task. If not specified, the defaultTaskList registered with the
	 * activity type will be used.
	 * <p>
	 * A task list for this activity task must be specified either as a default
	 * for the activity type or through this field. If neither this field is set
	 * nor a default task list was specified at registration time then a fault
	 * will be returned.
	 *
	 * @param taskListName
	 * @return
	 */
	public ActivityOptions taskList(final String taskList) {
		this.taskList = taskList;
		return this;
	}

	/**
	 * Optional. If set, specifies the priority with which the activity task is
	 * to be assigned to a worker. This overrides the defaultTaskPriority
	 * specified when registering the activity type using RegisterActivityType.
	 * Valid values are integers that range from Java's Integer.MIN_VALUE
	 * (-2147483648) to Integer.MAX_VALUE (2147483647). Higher numbers indicate
	 * higher priority.
	 * <p>
	 * For more information about setting task priority, see Setting Task
	 * Priority in the Amazon Simple Workflow Developer Guide.
	 *
	 * @param taskPriority
	 * @return
	 */
	public ActivityOptions taskPriority(final Integer taskPriority) {
		this.taskPriority = taskPriority;
		return this;
	}

	/**
	 * @param duration
	 *            maximum duration for the task from schedule to execution
	 *            complete before being timed-out
	 * @return
	 */
	public ActivityOptions maxTotalDuration(final Duration duration) {
		scheduleToCloseTimeout = toString(duration);
		return this;
	}

	/**
	 * @param duration
	 *            maximum duration for the task to start before being timed-out
	 * @return
	 */
	public ActivityOptions maxWaitingDuration(final Duration duration) {
		scheduleToStartTimeout = toString(duration);
		return this;
	}

	/**
	 * @param duration
	 *            maximum duration of the task execution before being timed-out
	 * @return
	 */
	public ActivityOptions maxExecutionDuration(final Duration duration) {
		startToCloseTimeout = toString(duration);
		return this;
	}

	/**
	 * @param duration
	 *            maximum duration of an heartbeat
	 * @return
	 */
	public ActivityOptions maxHeartbeatDuration(final Duration duration) {
		heartbeatTimeout = toString(duration);
		return this;
	}

	// GETTERS
	private String toString(final Duration duration) {
		return duration == null ? "NONE" : Long.toString(duration.getSeconds());
	}

	public String control() {
		return control;
	}

	public TaskList taskList() {
		return taskList == null ? null : new TaskList().withName(taskList);
	}

	public Integer taskPriority() {
		return taskPriority;
	}

	public String scheduleToCloseTimeout() {
		return scheduleToCloseTimeout;
	}

	public String scheduleToStartTimeout() {
		return scheduleToStartTimeout;
	}

	public String startToCloseTimeout() {
		return startToCloseTimeout;
	}

	public String heartbeatTimeout() {
		return heartbeatTimeout;
	}

}
