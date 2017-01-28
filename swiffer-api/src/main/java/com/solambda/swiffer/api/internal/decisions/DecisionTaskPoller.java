package com.solambda.swiffer.api.internal.decisions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.PollForDecisionTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.solambda.swiffer.api.internal.AbstractContextProviderImpl;

public class DecisionTaskPoller extends AbstractContextProviderImpl implements DecisionTaskContextProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DecisionTaskPoller.class);

	public DecisionTaskPoller(final AmazonSimpleWorkflow swf, final String domain, final String taskList,
			final String identity) {
		super(swf, domain, taskList, identity);
	}

	@Override
	public DecisionTaskContext get() {
		final DecisionTask task = task();
		if (task != null) {
			return new DecisionTaskContextImpl(this.swf, this.domain, task);
		} else {
			return null;
		}
	}

	private DecisionTask task() {
		try {
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
			return decisionTask;
		} catch (final Exception e) {
			throw new IllegalStateException(String.format("[%s:%s] Cannot poll decision taskList %s",
					this.domain, this.identity, this.taskList), e);
		}
	}

	@Override
	public void stop() {

	}

}
