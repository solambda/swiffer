package com.solambda.swiffer.api.internal.decisions;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
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
		LOGGER.debug("[{}:{}] Polling Decision task list '{}'", this.domain, this.identity, this.taskList);

		// FIXME: issue #5
		DecisionTask decisionTask = pollForDecisionTask(HistoryMode.EAGER);

		if (decisionTask == null || decisionTask.getTaskToken() == null) {
			LOGGER.debug("[{}:{}] no DecisionTask available in task list '{}'", this.domain, this.identity,
					this.taskList);
			return null;
		}
		LOGGER.debug("[{}:{}] DecisionTask received from '{}':{}", this.domain, this.identity, this.taskList,
				decisionTask);
		return new DecisionTaskContextImpl(this.swf, this.domain, decisionTask);
	}

	private DecisionTask pollForDecisionTask(HistoryMode mode) {
		switch (mode) {
			case EAGER:
				return pollForAllHistory();
			default:
				throw new UnsupportedOperationException("History mode " + mode + " is not supported.");
		}
	}

	private DecisionTask pollForAllHistory() {
		String nextPageToken = null;
		Collection<HistoryEvent> allEvents = new ArrayList<>();
		DecisionTask decisionTask;
		do {
			decisionTask = swf.pollForDecisionTask(new PollForDecisionTaskRequest()
														   .withDomain(domain)
														   .withTaskList(taskList == null ? null : new TaskList().withName(taskList))
														   .withReverseOrder(true)
														   .withIdentity(identity)
														   .withNextPageToken(nextPageToken));
			if (decisionTask != null) {
				nextPageToken = decisionTask.getNextPageToken();
				if (decisionTask.getEvents() != null)
				{
					allEvents.addAll(decisionTask.getEvents());
				}
			}
		}
		while (nextPageToken != null);

		if (decisionTask != null) {
			decisionTask.setEvents(allEvents);
		}

		return decisionTask;
	}
}
