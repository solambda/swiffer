package com.solambda.swiffer.api.internal.decisions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.PollForDecisionTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.solambda.swiffer.api.internal.AbstractTaskContextPoller;

public class DecisionTaskPoller extends AbstractTaskContextPoller<DecisionTaskContext> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DecisionTaskPoller.class);

	public DecisionTaskPoller(final AmazonSimpleWorkflow swf, final String domain, final String taskList,
			final String identity) {
		super(swf, domain, taskList, identity);
	}

	@Override
	protected DecisionTaskContext pollForTask() throws Exception {
		LOGGER.debug("[{}:{}] Polling DecisionTask list {}", this.domain, this.identity, this.taskList);
		final DecisionTask decisionTask = this.swf.pollForDecisionTask(new PollForDecisionTaskRequest()
				.withDomain(this.domain)
				.withTaskList(this.taskList == null ? null : new TaskList().withName(this.taskList))
				.withReverseOrder(false)
				.withIdentity(this.identity));
		// TODO auto poll more pages in the history
		// .withMaximumPageSize(maximumEventPageSize)
		// .withNextPageToken(nextPageToken)
		if (decisionTask == null || decisionTask.getTaskToken() == null) {
			LOGGER.debug("[{}:{}] no DecisionTask available in {}", this.domain, this.identity, this.taskList);
			return null;
		}
		LOGGER.debug("[{}:{}] DecisionTask received from {}:{}", this.domain, this.identity, this.taskList,
				decisionTask);
		return new DecisionTaskContextImpl(this.swf, this.domain, decisionTask);
	}

}
