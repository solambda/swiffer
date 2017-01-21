package com.solambda.swiffer.api.model;

import java.time.Duration;

import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.google.common.base.Preconditions;

public class Options {
	private static final TaskListIdentifier DEFAULT_TASK_LIST = new TaskListIdentifier("default");

	private ChildPolicy childTerminationPolicy;
	private TaskListIdentifier taskListIdentifier = new TaskListIdentifier(null);
	private Integer taskPriority = 0;

	private Duration maxExecutionDuration;
	private Duration maxDecisionTaskDuration;

	public static Options maxWorkflowDuration(final Duration maxWorkflowDuration) {
		return new Options(maxWorkflowDuration);
	}

	private Options(final Duration maxExecutionDuration) {
		this(maxExecutionDuration, null, ChildPolicy.ABANDON, null, null);
	}

	public Options(final Duration maxExecutionDuration, final Duration maxDecisionTaskDuration, final ChildPolicy childTerminationPolicy,
			final TaskListIdentifier taskListIdentifier, final Integer taskPriority) {
		super();
		Preconditions.checkNotNull(childTerminationPolicy, "child termination policy cannot be null");
		this.maxExecutionDuration = maxExecutionDuration;
		this.maxDecisionTaskDuration = maxDecisionTaskDuration;
		this.childTerminationPolicy = childTerminationPolicy;
		this.taskListIdentifier = taskListIdentifier;
		this.taskPriority = taskPriority;
	}

	public ChildPolicy getChildTerminationPolicy() {
		return childTerminationPolicy;
	}

	public TaskListIdentifier getTaskListIdentifier() {
		return taskListIdentifier == null ? DEFAULT_TASK_LIST : taskListIdentifier;
	}

	public String getTaskPriority() {
		return taskPriority == null ? null : Integer.toString(taskPriority);
	}

	public String getMaxExecutionDuration() {
		return maxExecutionDuration == null ? "0" : Long.toString(maxExecutionDuration.getSeconds());
	}

	public String getMaxDecisionTaskDuration() {
		return maxDecisionTaskDuration == null ? "NONE" : Long.toString(maxDecisionTaskDuration.getSeconds());
	}

	public TaskList getTaskList() {
		return new TaskList().withName(getTaskListIdentifier().getName());
	}
}
