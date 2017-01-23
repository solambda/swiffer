package com.solambda.swiffer.api.internal.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.PollForActivityTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;

public class ActivityTaskPoller implements ActivityTaskContextProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityTaskPoller.class);

	private final AmazonSimpleWorkflow client;
	private final String domain;
	private final TaskList taskList;
	private final String identity;

	public ActivityTaskPoller(final AmazonSimpleWorkflow client, final String domain, final String taskList,
			final String identity) {
		super();
		this.client = client;
		this.domain = domain;
		this.taskList = new TaskList().withName(taskList == null ? "default" : taskList);
		this.identity = identity;
	}

	@Override
	public ActivityTaskContext get() {
		final ActivityTask task = task();
		if (task != null) {
			return new ActivityTaskContextImpl(this.client, task);
		} else {
			return null;
		}
	}

	private ActivityTask task() {
		try {
			LOGGER.debug("[{}:{}] Polling taskList {}", this.domain, this.identity, this.taskList.getName());
			final PollForActivityTaskRequest pollForActivityTaskRequest = new PollForActivityTaskRequest()
					.withDomain(this.domain)
					.withIdentity(this.identity)
					.withTaskList(this.taskList);
			final ActivityTask activityTask = this.client.pollForActivityTask(pollForActivityTaskRequest);
			if (activityTask == null || activityTask.getTaskToken() == null) {
				LOGGER.debug("[{}:{}] no ActivityTask available in taskList {}", this.domain, this.identity,
						this.taskList.getName());
				return null;
			}
			LOGGER.debug("[{}:{}]  ActivityTask received from {}: {}", this.domain, this.identity,
					this.taskList.getName(),
					activityTask);
			return activityTask;
		} catch (final Exception e) {
			throw new IllegalStateException(String.format("[%s:%s] Cannot poll activity taskList %s",
					this.domain, this.identity, this.taskList.getName()), e);
		}
	}

	@Override
	public void stop() {

	}
}
