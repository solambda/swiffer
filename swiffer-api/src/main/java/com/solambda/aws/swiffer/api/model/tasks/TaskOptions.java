package com.solambda.aws.swiffer.api.model.tasks;

import java.time.Duration;

import com.amazonaws.services.simpleworkflow.model.TaskList;

public class TaskOptions {

	private String control;

	private String taskList;

	// MIN < 0 < MAX (max is higher priority)
	private String taskPriority;

	// NONE allowed
	private String scheduleToCloseTimeout;

	// NONE allowed
	private String scheduleToStartTimeout;

	// NONE allowed
	private String startToCloseTimeout;

	// NONE allowed
	private String heartbeatTimeout;

	public static TaskOptions inTaskList(final String taskListName) {
		return new TaskOptions().taskList(taskListName);
	}

	public TaskOptions control(final String control) {
		this.control = control;
		return this;
	}

	public TaskOptions taskList(final String taskList) {
		this.taskList = taskList;
		return this;
	}

	public TaskOptions taskPriority(final String taskPriority) {
		this.taskPriority = taskPriority;
		return this;
	}

	/**
	 * @param duration
	 *            maximum duration for the task from schedule to execution
	 *            complete before being timed-out
	 * @return
	 */
	public TaskOptions maxTotalDuration(final Duration duration) {
		this.scheduleToCloseTimeout = toString(duration);
		return this;
	}

	/**
	 * @param duration
	 *            maximum duration for the task to start before being timed-out
	 * @return
	 */
	public TaskOptions maxWaitingDuration(final Duration duration) {
		this.scheduleToStartTimeout = toString(duration);
		return this;
	}

	/**
	 * @param duration
	 *            maximum duration of the task execution before being timed-out
	 * @return
	 */
	public TaskOptions maxExecutionDuration(final Duration duration) {
		this.startToCloseTimeout = toString(duration);
		return this;
	}

	/**
	 * @param duration
	 *            maximum duration of an heartbeat
	 * @return
	 */
	public TaskOptions maxHeartbeatDuration(final Duration duration) {
		this.heartbeatTimeout = toString(duration);
		return this;
	}

	private String toString(final Duration duration) {
		return duration == null ? "NONE" : Long.toString(duration.getSeconds());
	}

	public String control() {
		return control;
	}

	public TaskList taskList() {
		return taskList == null ? null : new TaskList().withName(taskList);
	}

	public String taskPriority() {
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
