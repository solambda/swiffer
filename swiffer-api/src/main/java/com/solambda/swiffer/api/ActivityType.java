/**
 *
 */
package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
/**
 * Configure the registration of an activity.
 * <p>
 */
public @interface ActivityType {

	/**
	 * The registered name of the activity type.
	 *
	 * @return
	 */
	String name();

	/**
	 * The registered version of the activity type.
	 *
	 * @return
	 */
	String version();

	/**
	 * Specifies the default task list to use for scheduling tasks of this
	 * activity type.
	 * <p>
	 * This default task list is used if a task list is not provided when a task
	 * is scheduled through the ScheduleActivityTask Decision.
	 * <p>
	 * The default value is the string "default"
	 *
	 * @return
	 */
	String defaultTaskList() default "default";

	/**
	 * The default task priority to assign to the activity type. If not
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
	 * If set, specifies the default maximum duration that a worker can take to
	 * process tasks of this activity type. This default can be overridden when
	 * scheduling an activity task using the ScheduleActivityTask Decision.
	 * <p>
	 * The duration is specified in seconds; an integer greater than or equal to
	 * 0. The value -1 can be used to specify unlimited duration, which is the
	 * default value.
	 *
	 * @return
	 */
	int defaultTaskStartToCloseTimeout() default -1;

	/**
	 * If set, specifies the default maximum time before which a worker
	 * processing a task of this type must report progress by calling
	 * RecordActivityTaskHeartbeat. If the timeout is exceeded, the activity
	 * task is automatically timed out. This default can be overridden when
	 * scheduling an activity task using the ScheduleActivityTask Decision. If
	 * the activity worker subsequently attempts to record a heartbeat or
	 * returns a result, the activity worker receives an UnknownResource fault.
	 * In this case, Amazon SWF no longer considers the activity task to be
	 * valid; the activity worker should clean up the activity task.
	 * <p>
	 * The duration is specified in seconds; an integer greater than or equal to
	 * 0. The value -1 can be used to specify unlimited duration, which is the
	 * default value.
	 *
	 * @return
	 */
	int defaultTaskHeartbeatTimeout() default -1;

	/**
	 * If set, specifies the default maximum duration that a task of this
	 * activity type can wait before being assigned to a worker. This default
	 * can be overridden when scheduling an activity task using the
	 * ScheduleActivityTask Decision.
	 * <p>
	 * The duration is specified in seconds; an integer greater than or equal to
	 * 0. The value -1 can be used to specify unlimited duration, which is the
	 * default value.
	 *
	 * @return
	 */
	int defaultTaskScheduleToStartTimeout() default -1;

	/**
	 * If set, specifies the default maximum duration for a task of this
	 * activity type. This default can be overridden when scheduling an activity
	 * task using the ScheduleActivityTask Decision.
	 * <p>
	 * The duration is specified in seconds; an integer greater than or equal to
	 * 0. The value -1 can be used to specify unlimited duration, which is the
	 * default value.
	 *
	 * @return
	 */
	int defaultTaskScheduleToCloseTimeout() default -1;

	/**
	 * Textual description of the activity type. (1024 chars max)
	 *
	 * @return
	 */
	String description() default "";
}
