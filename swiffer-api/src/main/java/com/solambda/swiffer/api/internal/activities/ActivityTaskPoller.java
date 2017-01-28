package com.solambda.swiffer.api.internal.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.PollForActivityTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.solambda.swiffer.api.internal.AbstractContextProviderImpl;

public class ActivityTaskPoller extends AbstractContextProviderImpl implements ActivityTaskContextProvider {
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityTaskPoller.class);

	public ActivityTaskPoller(final AmazonSimpleWorkflow client, final String domain, final String taskList,
			final String identity) {
		super(client, domain, taskList, identity);
	}

	@Override
	public ActivityTaskContext get() {
		final ActivityTask task = task();
		if (task != null) {
			return new ActivityTaskContextImpl(this.swf, task);
		} else {
			return null;
		}
	}

	private ActivityTask task() {
		try {
			LOGGER.debug("[{}:{}] Polling ActivityTask list {}", this.domain, this.identity, this.taskList);
			final PollForActivityTaskRequest pollForActivityTaskRequest = new PollForActivityTaskRequest()
					.withDomain(this.domain)
					.withIdentity(this.identity)
					.withTaskList(new TaskList().withName(this.taskList));
			final ActivityTask activityTask = this.swf.pollForActivityTask(pollForActivityTaskRequest);
			if (activityTask == null || activityTask.getTaskToken() == null) {
				LOGGER.debug("[{}:{}] no ActivityTask available in {}", this.domain, this.identity, this.taskList);
				return null;
			}
			LOGGER.debug("[{}:{}] ActivityTask received from {}:{}", this.domain, this.identity, this.taskList,
					activityTask);
			return activityTask;
		} catch (final Exception e) {
			throw new IllegalStateException(String.format("[%s:%s] Cannot poll ActivityTask list %s",
					this.domain, this.identity, this.taskList), e);
		}
	}

	@Override
	public void stop() {

	}
}
