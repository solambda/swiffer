package com.solambda.swiffer.api.internal.decisions;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.EventType;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.google.common.collect.Lists;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.mapper.DataMapper;

public class DecisionTaskContextImpl implements DecisionTaskContext {

	private AmazonSimpleWorkflow swf;
	private DecisionTask decisionTask;
	private WorkflowHistory history;
	private String domain;
    private final DataMapper dataMapper;

    public DecisionTaskContextImpl(final AmazonSimpleWorkflow swf, final String domain,
                                   final DecisionTask decisionTask,
                                   DataMapper dataMapper) {
        super();
		this.swf = swf;
		this.decisionTask = decisionTask;
		this.domain = domain;
		this.dataMapper = dataMapper;
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

	@Override
	public String workflowId() {
		return decisionTask.getWorkflowExecution().getWorkflowId();
	}

	@Override
    public boolean hasMarker(String markerName) {
        return decisionTask.getEvents().stream().anyMatch(isMarkerRecordedEvent(markerName));
    }

	@Override
    public <T> Optional<T> getMarkerDetails(String markerName, Class<T> type) {
        return decisionTask.getEvents().stream()
                           .filter(isMarkerRecordedEvent(markerName))
                           .findFirst()
                           .map(deserialize(type));
	}

	@Override
	public boolean isCancelRequested() {
		return decisionTask.getEvents().stream()
						   .anyMatch(historyEvent -> historyEvent.getEventType().equals(EventType.WorkflowExecutionCancelRequested.name()));
	}

	@Override
	public String toString() {
		return "DecisionTaskContextImpl [domain=" + this.domain
				+ ", workflowType=" + workflowType() + "]";
	}

    private static Predicate<HistoryEvent> isMarkerRecordedEvent(String markerName) {
        return event -> event.getMarkerRecordedEventAttributes() != null
                        && event.getMarkerRecordedEventAttributes().getMarkerName().equals(markerName);
    }

    private <T> Function<HistoryEvent, T> deserialize(Class<T> type) {
        return event -> dataMapper.deserialize(event.getMarkerRecordedEventAttributes().getDetails(), type);
    }
}
