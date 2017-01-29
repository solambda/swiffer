package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.ChildPolicy;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
/**
 * Configure the registration of a workflow type.
 * <p>
 */
public @interface WorkflowType {
	/**
	 * The registered name of the workflow type.
	 *
	 * @return
	 */
	String name();

	/**
	 * The registered version of the workflow type.
	 *
	 * @return
	 */
	String version();

	/**
	 * Specifies the default task list to use for scheduling decision tasks for
	 * executions of this workflow type.
	 * <p>
	 * This default is used only if a task list is not provided when starting
	 * the execution through the StartWorkflowExecution Action or
	 * StartChildWorkflowExecution Decision.
	 * <p>
	 * The default value is the string "default"
	 *
	 * @return
	 */
	String defaultTaskList() default "default";

	/**
	 * Specifies the default policy to use for the child workflow executions
	 * when a workflow execution of this type is terminated, by calling the
	 * TerminateWorkflowExecution action explicitly or due to an expired
	 * timeout. This default can be overridden when starting a workflow
	 * execution using the StartWorkflowExecution action or the
	 * StartChildWorkflowExecution Decision.
	 * <p>
	 * The supported child policies are:
	 * <ul>
	 * <li>TERMINATE: the child executions will be terminated.
	 * <li>REQUEST_CANCEL: a request to cancel will be attempted for each child
	 * execution by recording a WorkflowExecutionCancelRequested event in its
	 * history. It is up to the decider to take appropriate actions when it
	 * receives an execution history with this event.
	 * <li>ABANDON: no action will be taken. The child executions will continue
	 * to run.
	 * </ul>
	 * <p>
	 * If not set, {@link ChildPolicy#ABANDON} is used.
	 *
	 * @return the default {@link ChildPolicy} to apply
	 */
	ChildPolicy defaultChildPolicy() default ChildPolicy.ABANDON;

	/**
	 * If set, specifies the default maximum duration for executions of this
	 * workflow type, in seconds.
	 * <p>
	 * You can override this default when starting an execution through the
	 * StartWorkflowExecution Action or StartChildWorkflowExecution Decision.
	 * <p>
	 * The default value is {@link Integer#MAX_VALUE}, meaning that no limit is
	 * provided, and the duration is the maximum authorized by AWS SWF, i.e 1
	 * year, after which, the workflow execution times out.
	 *
	 * @return
	 */
	int defaultExecutionStartToCloseTimeout() default Integer.MAX_VALUE;

	/**
	 * The default task priority to assign to the workflow type. If not
	 * assigned, then "0" will be used. Valid values are integers that range
	 * from Java's Integer.MIN_VALUE (-2147483648) to Integer.MAX_VALUE
	 * (2147483647). Higher numbers indicate higher priority.
	 * <p>
	 * For more information about setting task priority, see Setting Task
	 * Priority in the Amazon Simple Workflow Developer Guide.
	 *
	 * @return
	 */
	int defaultTaskPriority() default 0;

	/**
	 * If set, specifies the default maximum duration of decision tasks for this
	 * workflow type, in seconds.
	 * <p>
	 * This default can be overridden when starting a workflow execution using
	 * the StartWorkflowExecution action or the StartChildWorkflowExecution
	 * Decision.
	 * <p>
	 * The duration is specified in seconds; an integer greater than or equal to
	 * 0. The value -1 can be used to specify unlimited duration, which is the
	 * default value.
	 *
	 * @return
	 */
	int defaultTaskStartToCloseTimeout() default -1;

	/**
	 * Textual description of the workflow type. (1024 chars max)
	 *
	 * @return
	 */
	String description() default "";

	String defaultLambdaRole() default "";

}
