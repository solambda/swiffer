package com.solambda.swiffer.api.registries;

import java.time.Duration;
import java.util.Objects;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.*;
import com.solambda.swiffer.api.model.TaskType;
import com.solambda.swiffer.api.model.tasks.TaskOptions;

public class TaskRegistry {

	private static final Duration INFINITE_DURATION = null;

	/**
	 * The default options:
	 * <ul>
	 * <li>all timeouts are set to NONE (i.e no timeout)
	 * <li>TaskPriotiy is 0 (default)
	 * <li>tasklist name is "default"
	 * </ul>
	 */
	public static final TaskOptions DEFAULT_OPTIONS = new TaskOptions()
			.taskList("default")
			.taskPriority("0")
			.maxExecutionDuration(INFINITE_DURATION)
			.maxHeartbeatDuration(INFINITE_DURATION)
			.maxTotalDuration(INFINITE_DURATION)
			.maxWaitingDuration(INFINITE_DURATION);

	private AmazonSimpleWorkflow swf;

	public TaskRegistry(final AmazonSimpleWorkflow swf) {
		super();
		this.swf = swf;
	}

	public void create(final String domainName, final TaskType taskType, final String taskDescription) {
		create(domainName, taskType, taskDescription, DEFAULT_OPTIONS);
	}

	public void create(final String domain, final TaskType taskType, final String description, final TaskOptions options) {
		TaskOptions opts = options == null ? DEFAULT_OPTIONS : options;
		try {
			swf.registerActivityType(new RegisterActivityTypeRequest()
					.withDomain(domain)
					.withName(taskType.name())
					.withVersion(taskType.version())
					.withDescription(description)
					.withDefaultTaskList(opts.taskList())
					.withDefaultTaskPriority(opts.taskPriority())
					.withDefaultTaskScheduleToStartTimeout(opts.scheduleToStartTimeout())
					.withDefaultTaskStartToCloseTimeout(opts.startToCloseTimeout())
					.withDefaultTaskScheduleToCloseTimeout(opts.scheduleToCloseTimeout())
					.withDefaultTaskHeartbeatTimeout(opts.heartbeatTimeout()));
			// return new Activity(activityName, activityVersion);
		} catch (TypeAlreadyExistsException e) {
			// return new Activity(activityName, activityVersion);
			throw new IllegalStateException(String.format("cannot register the activity %s v=%s in domain %s", taskType.name(), taskType.version(), domain), e);
		} catch (UnknownResourceException e) {
			throw new IllegalStateException(String.format("cannot register the activity %s v=%s in domain %s", taskType.name(), taskType.version(), domain), e);
		}
	}

	/**
	 */
	public void delete(final String domain, final TaskType taskType) {
		try {
			swf.deprecateActivityType(new DeprecateActivityTypeRequest()
					.withDomain(domain)
					.withActivityType(new ActivityType().withName(taskType.name()).withVersion(taskType.version())));
		} catch (TypeDeprecatedException e) {
		} catch (UnknownResourceException e) {
		}
	}

	public boolean exists(final String domain, final TaskType taskType) {
		try {
			ActivityTypeDetail details = swf.describeActivityType(new DescribeActivityTypeRequest()
					.withDomain(domain)
					.withActivityType(new ActivityType().withName(taskType.name()).withVersion(taskType.version())));
			return Objects.equals(details.getTypeInfo().getStatus(), "REGISTERED");
		} catch (UnknownResourceException e) {
			return false;
		}
	}

}
