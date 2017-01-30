package com.solambda.swiffer.api;

import java.time.Duration;

import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.internal.TaskListIdentifier;

public class WorkflowOptions {
	// private static final TaskListIdentifier DEFAULT_TASK_LIST = new
	// TaskListIdentifier("default");

	private static final Duration ONE_YEAR = Duration.ofDays(365);
	public static final Duration UNLIMITED = ONE_YEAR.plusDays(1);

	private ChildPolicy childTerminationPolicy;
	private TaskListIdentifier taskListIdentifier;
	private Integer taskPriority;

	private Duration maxExecutionDuration;
	private Duration maxDecisionTaskDuration;

	// lambda role

	public WorkflowOptions() {
	}

	/**
	 * If set, specifies the policy to use for the child workflow executions of
	 * this workflow execution if it is terminated, by calling the
	 * TerminateWorkflowExecution action explicitly or due to an expired
	 * timeout. This policy overrides the default child policy specified when
	 * registering the workflow type using RegisterWorkflowType.
	 * <p>
	 * <ul>
	 * <li>TERMINATE: the child executions will be terminated.<br>
	 * <li>REQUEST_CANCEL: a request to cancel will be attempted for each child
	 * execution by recording a WorkflowExecutionCancelRequested event in its
	 * history. It is up to the decider to take appropriate actions when it
	 * receives an execution history with this event.
	 * <li>ABANDON: no action will be taken. The child executions will continue
	 * to run.
	 * </ul>
	 * <p>
	 * A child policy for this workflow execution must be specified either as a
	 * default for the workflow type or through this parameter. If neither this
	 * parameter is set nor a default child policy was specified at registration
	 * time then a fault will be returned.
	 *
	 * @param childTerminationPolicy
	 * @return
	 */
	public WorkflowOptions childTerminationPolicy(final ChildPolicy childTerminationPolicy) {
		this.childTerminationPolicy = childTerminationPolicy;
		return this;
	}

	/**
	 * The total duration for this workflow execution. This overrides the
	 * defaultExecutionStartToCloseTimeout specified when registering the
	 * workflow type.
	 * <p>
	 * There is a one-year max limit on the time that a workflow execution can
	 * run.
	 * <p>
	 * An execution start-to-close timeout must be specified either through this
	 * parameter or as a default when the workflow type is registered. If
	 * neither this parameter nor a default execution start-to-close timeout is
	 * specified, a fault is returned.
	 *
	 * @param maxWorkflowDuration
	 * @return
	 */
	public WorkflowOptions maxWorkflowDuration(final Duration maxWorkflowDuration) {
		Preconditions.checkArgument(maxWorkflowDuration == null || maxWorkflowDuration.compareTo(ONE_YEAR) <= 0,
				"Max workflow duration must be less than 1 year!");
		this.maxExecutionDuration = maxWorkflowDuration;
		return this;
	}

	/**
	 * Specifies the maximum duration of decision tasks for this workflow
	 * execution. This parameter overrides the defaultTaskStartToCloseTimout
	 * specified when registering the workflow type using RegisterWorkflowType.
	 * <p>
	 * The value {@link WorkflowOptions#UNLIMITED} can be used to specify
	 * unlimited duration.
	 * <p>
	 * Note A task start-to-close timeout for this workflow execution must be
	 * specified either as a default for the workflow type or through this
	 * parameter. If neither this parameter is set nor a default task
	 * start-to-close timeout was specified at registration time then a fault
	 * will be returned.
	 *
	 * @param maxDecisionTaskDuration
	 * @return
	 */
	public WorkflowOptions maxDecisionTaskDuration(final Duration maxDecisionTaskDuration) {
		this.maxDecisionTaskDuration = maxDecisionTaskDuration;
		return this;
	}

	/**
	 * The task list to use for the decision tasks generated for this workflow
	 * execution. This overrides the defaultTaskList specified when registering
	 * the workflow type.
	 * <p>
	 * Note A task list for this workflow execution must be specified either as
	 * a default for the workflow type or through this parameter. If neither
	 * this parameter is set nor a default task list was specified at
	 * registration time then a fault will be returned.
	 * <p>
	 * The specified string must not start or end with whitespace. It must not
	 * contain a : (colon), / (slash), | (vertical bar), or any control
	 * characters (\u0000-\u001f | \u007f - \u009f). Also, it must not contain
	 * the literal string "arn".
	 *
	 * @param taskList
	 * @return
	 */
	public WorkflowOptions taskList(final String taskList) {
		this.taskListIdentifier = taskList == null ? null : new TaskListIdentifier(taskList);
		return this;
	}

	/**
	 * The task priority to use for this workflow execution. This will override
	 * any default priority that was assigned when the workflow type was
	 * registered. If not set, then the default task priority for the workflow
	 * type will be used.
	 * <p>
	 * Valid values are integers that range from Java's Integer.MIN_VALUE
	 * (-2147483648) to Integer.MAX_VALUE (2147483647). Higher numbers indicate
	 * higher priority.
	 *
	 * For more information about setting task priority, see Setting Task
	 * Priority in the Amazon Simple Workflow Developer Guide.
	 *
	 * @param taskPriority
	 * @return
	 */
	public WorkflowOptions taskPriority(final Integer taskPriority) {
		this.taskPriority = taskPriority;
		return this;
	}

	public ChildPolicy getChildTerminationPolicy() {
		return this.childTerminationPolicy;
	}

	public TaskListIdentifier getTaskListIdentifier() {
		return this.taskListIdentifier;
	}

	public String getTaskPriority() {
		return this.taskPriority == null ? null : Integer.toString(this.taskPriority);
	}

	public String getMaxExecutionDuration() {
		return this.maxExecutionDuration == null ? null : Long.toString(this.maxExecutionDuration.getSeconds());
	}

	public String getMaxDecisionTaskDuration() {
		return this.maxDecisionTaskDuration == null ? null
				: UNLIMITED.equals(this.maxDecisionTaskDuration) ? "NONE"
						: Long.toString(this.maxDecisionTaskDuration.getSeconds());
	}

	public TaskList getTaskList() {
		return this.taskListIdentifier == null ? null : new TaskList().withName(getTaskListIdentifier().getName());
	}
}
