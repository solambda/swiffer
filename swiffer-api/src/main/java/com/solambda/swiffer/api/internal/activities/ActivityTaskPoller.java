package com.solambda.swiffer.api.internal.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.PollForActivityTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.solambda.swiffer.api.internal.AbstractTaskContextPoller;

public class ActivityTaskPoller
		extends AbstractTaskContextPoller<ActivityTaskContext> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityTaskPoller.class);

	public ActivityTaskPoller(final AmazonSimpleWorkflow swf, final String domain, final String taskList,
			final String identity) {
		super(swf, domain, taskList, identity);
	}

	@Override
	protected ActivityTaskContext pollForTask() throws Exception {
		LOGGER.debug("[{}:{}] Polling Activity task list '{}'", this.domain, this.identity, this.taskList);
		final PollForActivityTaskRequest pollForActivityTaskRequest = new PollForActivityTaskRequest()
				.withDomain(this.domain)
				.withIdentity(this.identity)
				.withTaskList(new TaskList().withName(this.taskList));
		final ActivityTask activityTask = this.swf.pollForActivityTask(pollForActivityTaskRequest);
		if (activityTask == null || activityTask.getTaskToken() == null) {
			LOGGER.debug("[{}:{}] no ActivityTask available in task list '{}'", this.domain, this.identity,
					this.taskList);
			return null;
		}
		LOGGER.debug("[{}:{}] ActivityTask received from '{}':{}", this.domain, this.identity, this.taskList,
				activityTask);
		return new ActivityTaskContextImpl(this.swf, activityTask);

	}

}
