package com.solambda.swiffer.api.internal.decisions;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.google.common.collect.Lists;
import com.solambda.swiffer.api.internal.VersionedName;

public class DecisionTaskContextImpl implements DecisionTaskContext {

	private AmazonSimpleWorkflow swf;
	private DecisionTask decisionTask;
	private WorkflowHistory history;
	private String domain;

	public DecisionTaskContextImpl(final AmazonSimpleWorkflow swf, final String domain, final DecisionTask decisionTask) {
		super();
		this.swf = swf;
		this.decisionTask = decisionTask;
		this.domain = domain;
	}

	@Override
	public WorkflowHistory history() {
		if (this.history == null) {
			final List<HistoryEvent> events = prefetchAllHistoryEvents(this.decisionTask);
			this.history = new WorkflowHistoryImpl(events);
		}
		return this.history;
	}

	private List<HistoryEvent> prefetchAllHistoryEvents(final DecisionTask decisionTask) {
		// TODO: load all the pages if more than one
		return decisionTask.getEvents();
	}

	@Override
	public String taskToken() {
		return this.decisionTask.getTaskToken();
	}

	@Override
	public List<WorkflowEvent> newEvents() {
		final Long previousStartedEventId = this.decisionTask.getPreviousStartedEventId();
		return Lists.reverse(history().events().stream()
				.filter(e -> e.id().compareTo(previousStartedEventId) > 0)
				.collect(Collectors.toList()));

	}

	@Override
	public VersionedName workflowType() {
		final WorkflowType workflowType = this.decisionTask.getWorkflowType();
		return new VersionedName(workflowType.getName(), workflowType.getVersion());
	}
}
