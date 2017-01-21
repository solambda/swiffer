package com.solambda.aws.swiffer.api.model.decider.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.solambda.aws.swiffer.api.model.DomainIdentifier;
import com.solambda.aws.swiffer.api.model.WorkflowHistory;
import com.solambda.aws.swiffer.api.model.WorkflowTypeId;
import com.solambda.aws.swiffer.api.model.decider.DecisionContext;
import com.solambda.aws.swiffer.api.model.decider.WorkflowEvent;
import com.solambda.aws.swiffer.api.model.impl.WorkflowHistoryImpl;
import com.google.common.collect.Lists;

public class DecisionContextImpl implements DecisionContext {

	private AmazonSimpleWorkflow swf;
	private DecisionTask decisionTask;
	private WorkflowHistory history;
	private String domain;

	public DecisionContextImpl(final AmazonSimpleWorkflow swf, final String domain, final DecisionTask decisionTask) {
		super();
		this.swf = swf;
		this.decisionTask = decisionTask;
		this.domain = domain;
	}

	@Override
	public WorkflowHistory history() {
		if (history == null) {
			List<HistoryEvent> events = prefetchAllHistoryEvents(decisionTask);
			history = new WorkflowHistoryImpl(events);
		}
		return history;
	}

	private List<HistoryEvent> prefetchAllHistoryEvents(final DecisionTask decisionTask) {
		// TODO: load all the pages if more than one
		return decisionTask.getEvents();
	}

	@Override
	public String decisionTaskId() {
		return decisionTask.getTaskToken();
	}

	@Override
	public List<WorkflowEvent> newEvents() {
		Long previousStartedEventId = decisionTask.getPreviousStartedEventId();
		return Lists.reverse(history().events().stream()
				.filter(e -> e.id().compareTo(previousStartedEventId) > 0)
				.collect(Collectors.toList()));

	}

	@Override
	public WorkflowTypeId workflowType() {
		WorkflowType workflowType = decisionTask.getWorkflowType();
		return new WorkflowTypeId(new DomainIdentifier(domain), workflowType.getName(), workflowType.getVersion());
	}
}
