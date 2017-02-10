package com.solambda.swiffer.api.internal.decisions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.mapper.DataMapper;

public class WorkflowTemplateImpl implements WorkflowTemplate {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowTemplateImpl.class);

	private VersionedName workflowType;
	private final EventHandlerRegistry eventHandlerRegistry;
	private final DataMapper dataMapper;

	public WorkflowTemplateImpl(final VersionedName workflowType,
								final EventHandlerRegistry eventHandlerRegistry,
								DataMapper dataMapper) {
		super();
		this.workflowType = workflowType;
		this.eventHandlerRegistry = eventHandlerRegistry;
		this.dataMapper = dataMapper;
	}

	@Override
	public VersionedName getWorkflowType() {
		return this.workflowType;
	}

	@Override
	public Decisions decide(final DecisionTaskContext decisionContext) throws DecisionTaskExecutionException {
		final Decisions decisions = new DecisionsImpl(dataMapper);
		final List<WorkflowEvent> newEvents = decisionContext.newEvents();
		LOGGER.debug("processing {} new events", newEvents.size());
		for (final WorkflowEvent event : newEvents) {
			LOGGER.debug("processing new event {}", event);
			final EventContext eventContext = new EventContextImpl(decisionContext, event);
			final EventHandlerType eventType = new EventHandlerType(eventContext.event().type(), eventContext.name());
			// in some case, we can warn the user if there is no event handler
			// for that event type
			final EventHandler eventHandler = this.eventHandlerRegistry.get(eventType);
			processEventHandler(eventHandler, eventContext, decisions);
		}
		return decisions;
	}

	private void processEventHandler(final EventHandler eventHandler, final EventContext eventContext,
			final Decisions decisions) throws DecisionTaskExecutionException {
		if (eventHandler == null) {
			doDefaultEventHandler(eventContext);
		} else {
			try {
				eventHandler.handleEvent(eventContext, decisions);
			} catch (final DecisionTaskExecutionException e) {
				// TODO let's make the failing behavior configurable by the
				// a failure here should
				// - an error here is a development bug:
				// ==> just log and hope the dev team will redeploy a bug fix
				// before the DecisionTask timeouts !
				// this.LOGGER.error("failure during event handling of event
				// {}", eventContext.event(), e);
				throw e;
			}
		}
	}

	private void doDefaultEventHandler(final EventContext eventContext) {
		// do
		LOGGER.debug("no event handler defined for {}", eventContext.event());
	}

	@Override
	public String toString() {
		return getWorkflowType().toString();
	}

}
