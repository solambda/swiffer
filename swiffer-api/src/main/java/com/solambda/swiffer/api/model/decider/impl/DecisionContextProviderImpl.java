package com.solambda.swiffer.api.model.decider.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.PollForDecisionTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.solambda.swiffer.api.model.decider.DecisionContext;
import com.solambda.swiffer.api.model.decider.DecisionContextProvider;

public class DecisionContextProviderImpl extends AbstractContextProviderImpl implements DecisionContextProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DecisionContextProviderImpl.class);

	public DecisionContextProviderImpl(final AmazonSimpleWorkflow swf, final String domain, final String taskList, final String identity) {
		super(swf, domain, taskList, identity);
	}

	@Override
	public DecisionContext get() {
		LOGGER.debug("poller '{}' is polling for decision task on task list {}", identity, taskList);
		DecisionTask task = task();
		if (task != null) {
			LOGGER.debug("poller '{}' received a decision task from task list {}", identity, taskList);
			return new DecisionContextImpl(swf, domain, task);
		} else {
			LOGGER.debug("poller '{}' got no task on task list {}", identity, taskList);
			return null;
		}
	}

	@Override
	public void stop() {

	}

	private DecisionTask task() {
		try {
			DecisionTask decisionTask = swf.pollForDecisionTask(new PollForDecisionTaskRequest()
					.withDomain(domain)
					.withTaskList(taskList == null ? null : new TaskList().withName(taskList))
					.withReverseOrder(false)
					.withIdentity(identity)
					// defaults are the maximum allowed
					// TODO auto poll more pages in the history
					// .withMaximumPageSize(maximumEventPageSize)
					// .withNextPageToken(nextPageToken)

					// .withSdkClientExecutionTimeout((int) timeout.toMillis())
					// .withSdkRequestTimeout((int) timeout.toMillis())
					);
			if (decisionTask == null || decisionTask.getTaskToken() == null) {
				return null;
			}
			return decisionTask;
		} catch (Exception e) {
			throw new IllegalStateException("Cannot poll for decision context", e);
		}
	}

}
