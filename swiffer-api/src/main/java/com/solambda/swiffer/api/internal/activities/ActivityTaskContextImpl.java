package com.solambda.swiffer.api.internal.activities;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.ActivityType;
import com.amazonaws.services.simpleworkflow.model.GetWorkflowExecutionHistoryRequest;
import com.amazonaws.services.simpleworkflow.model.History;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.decisions.WorkflowHistory;
import com.solambda.swiffer.api.internal.decisions.WorkflowHistoryImpl;

public class ActivityTaskContextImpl implements ActivityTaskContext {

	private AmazonSimpleWorkflow swf;
	private ActivityTask task;
	private String domain;

	public ActivityTaskContextImpl(final AmazonSimpleWorkflow client, final ActivityTask task) {
		super();
		this.swf = client;
		this.task = task;
	}

	@Override
	public String taskToken() {
		return this.task.getTaskToken();
	}

	@Override
	public String input() {
		return this.task.getInput();
	}

	@Override
	public String activityId() {
		return this.task.getActivityId();
	}

	@Override
	public VersionedName activityType() {
		final ActivityType activityType = this.task.getActivityType();
		return new VersionedName(activityType.getName(), activityType.getVersion());
	}

	@Override
	public WorkflowHistory history() {
		WorkflowHistory history = null;
		do {
			history = fetchHistory();
		} while (taskIsNotInHistory(history) && sleep(Duration.ofSeconds(2)));
		return history;
	}

	private boolean sleep(final Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		} catch (final InterruptedException e) {
		}
		return true;
	}

	private boolean taskIsNotInHistory(final WorkflowHistory history) {
		return history.getEventById(this.task.getStartedEventId()) == null;
	}

	private WorkflowHistory fetchHistory() {
		final List<HistoryEvent> allEvents = new ArrayList<HistoryEvent>();
		String nextPageToken = null;
		do {
			final History history = this.swf.getWorkflowExecutionHistory(new GetWorkflowExecutionHistoryRequest()
					.withDomain(this.domain)
					.withExecution(this.task.getWorkflowExecution())
					.withReverseOrder(true)
					.withNextPageToken(nextPageToken));
			allEvents.addAll(history.getEvents());
			nextPageToken = history.getNextPageToken();
		} while (nextPageToken != null);
		return new WorkflowHistoryImpl(allEvents);
	}
}
