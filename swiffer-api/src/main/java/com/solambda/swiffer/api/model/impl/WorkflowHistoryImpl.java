package com.solambda.swiffer.api.model.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.model.WorkflowHistory;
import com.solambda.swiffer.api.model.decider.WorkflowEvent;

public class WorkflowHistoryImpl implements WorkflowHistory {

	private List<WorkflowEvent> events;

	public WorkflowHistoryImpl(final List<HistoryEvent> events) {
		super();
		Preconditions.checkArgument(events != null, "events must not be null");
		Preconditions.checkArgument(events.size() > 0, "event list is empty");
		this.events = events.stream()
				.map(e -> new WorkflowEvent(e, this))
				.sorted()
				.collect(Collectors.toList());
		Long oldestEventId = this.events.get(events.size() - 1).id();
		Preconditions.checkState(oldestEventId == 1L, "history is not complete ! the oldest event is " + oldestEventId + " but should be 1");
	}

	@Override
	public List<WorkflowEvent> events() {
		return events;
	}

	@Override
	public WorkflowEvent getEventById(final Long id) {
		Preconditions.checkArgument(id > 0, "cannot get event id " + id);
		int eventIdToIndex = events.size() - id.intValue();
		return eventIdToIndex >= 0 && eventIdToIndex < events.size() ? events.get(eventIdToIndex) : null;
	}
}
