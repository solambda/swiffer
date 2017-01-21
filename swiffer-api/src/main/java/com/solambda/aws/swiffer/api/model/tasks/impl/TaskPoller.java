package com.solambda.aws.swiffer.api.model.tasks.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.PollForActivityTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.solambda.aws.swiffer.api.model.tasks.TaskContext;
import com.solambda.aws.swiffer.api.model.tasks.TaskContextProvider;

public class TaskPoller implements TaskContextProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskPoller.class);

	private final AmazonSimpleWorkflow client;
	private final String domain;
	private final TaskList taskList;
	private final String identity;

	public TaskPoller(final AmazonSimpleWorkflow client, final String domain, final String taskList, final String identity) {
		super();
		this.client = client;
		this.domain = domain;
		this.taskList = new TaskList().withName(taskList == null ? "default" : taskList);
		this.identity = identity;
	}

	@Override
	public TaskContext get() {
		ActivityTask task = task();
		if (task != null) {
			return new TaskContextImpl(client, task);
		} else {
			return null;
		}
	}

	private ActivityTask task() {
		try {
			LOGGER.debug("['{}':'{}'] Polling taskList '{}'", domain, identity, taskList.getName());
			PollForActivityTaskRequest pollForActivityTaskRequest = new PollForActivityTaskRequest()
					.withDomain(domain)
					.withIdentity(identity)
					.withTaskList(taskList);
			ActivityTask activityTask = client.pollForActivityTask(pollForActivityTaskRequest);
			if (activityTask == null || activityTask.getTaskToken() == null) {
				LOGGER.debug("['{}'] no task available");
				return null;
			}
			LOGGER.debug("['{}'] Task received: {}", activityTask);
			return activityTask;
		} catch (Exception e) {
			throw new IllegalStateException("Cannot poll for task context", e);
		}
	}

	@Override
	public void stop() {

	}
}
