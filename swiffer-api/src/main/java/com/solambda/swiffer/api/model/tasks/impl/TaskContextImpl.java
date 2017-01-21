package com.solambda.swiffer.api.model.tasks.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.*;
import com.solambda.swiffer.api.model.TaskType;
import com.solambda.swiffer.api.model.WorkflowHistory;
import com.solambda.swiffer.api.model.impl.WorkflowHistoryImpl;
import com.solambda.swiffer.api.model.tasks.TaskContext;

public class TaskContextImpl implements TaskContext {

	private AmazonSimpleWorkflow swf;
	private ActivityTask task;
	private String domain;

	public TaskContextImpl(final AmazonSimpleWorkflow client, final ActivityTask task) {
		super();
		this.swf = client;
		this.task = task;
	}

	@Override
	public String contextId() {
		return task.getTaskToken();
	}

	@Override
	public String input() {
		return task.getInput();
	}

	@Override
	public String taskId() {
		return task.getActivityId();
	}

	@Override
	public TaskType taskType() {
		ActivityType activityType = task.getActivityType();
		return new TaskType(activityType.getName(), activityType.getVersion());
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
		} catch (InterruptedException e) {
		}
		return true;
	}

	private boolean taskIsNotInHistory(final WorkflowHistory history) {
		return history.getEventById(task.getStartedEventId()) == null;
	}

	private WorkflowHistory fetchHistory() {
		List<HistoryEvent> allEvents = new ArrayList<HistoryEvent>();
		String nextPageToken = null;
		do {
			History history = swf.getWorkflowExecutionHistory(new GetWorkflowExecutionHistoryRequest()
					.withDomain(domain)
					.withExecution(task.getWorkflowExecution())
					.withReverseOrder(true)
					.withNextPageToken(nextPageToken)
					);
			allEvents.addAll(history.getEvents());
			nextPageToken = history.getNextPageToken();
		} while (nextPageToken != null);
		return new WorkflowHistoryImpl(allEvents);
	}
}
