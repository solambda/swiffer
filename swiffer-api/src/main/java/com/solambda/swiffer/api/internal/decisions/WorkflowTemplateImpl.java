package com.solambda.swiffer.api.internal.decisions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.events.HasTimerId;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class WorkflowTemplateImpl implements WorkflowTemplate {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowTemplateImpl.class);

	private VersionedName workflowType;
	private final EventHandlerRegistry eventHandlerRegistry;
	private final DataMapper dataMapper;
	private final DurationTransformer durationTransformer;
	private final RetryPolicy globalRetryPolicy;

	public WorkflowTemplateImpl(final VersionedName workflowType,
								final EventHandlerRegistry eventHandlerRegistry,
								DataMapper dataMapper,
								DurationTransformer durationTransformer,
								RetryPolicy globalRetryPolicy) {
        this.workflowType = workflowType;
		this.eventHandlerRegistry = eventHandlerRegistry;
		this.dataMapper = dataMapper;
		this.durationTransformer = durationTransformer;
		this.globalRetryPolicy = globalRetryPolicy;
	}

	@Override
	public VersionedName getWorkflowType() {
		return this.workflowType;
	}

	@Override
	public Decisions decide(final DecisionTaskContext decisionContext) throws DecisionTaskExecutionException {
		final Decisions decisions = new DecisionsImpl(dataMapper, durationTransformer, globalRetryPolicy);
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
			doDefaultEventHandler(eventContext, decisions);
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

	private void doDefaultEventHandler(final EventContext eventContext, Decisions decisions) throws DecisionTaskExecutionException {
        EventType type = eventContext.event().type();
        EventHandler eventHandler = null;
        switch (type) {
            case ActivityTaskFailed:
                eventHandler = eventHandlerRegistry.getDefaultFailedActivityHandler();
                break;
            case ActivityTaskTimedOut:
                eventHandler = eventHandlerRegistry.getDefaultTimedOutActivityHandler();
                break;
            case TimerFired:
                String timerId = ((HasTimerId) eventContext).timerId();
                eventHandler = eventHandlerRegistry.getDefaultRetryTimerFiredHandler(timerId);
                break;
        }
        if (eventHandler != null) {
            eventHandler.handleEvent(eventContext, decisions);
        } else {
            // do
            LOGGER.debug("no event handler defined for {}", eventContext.event());
        }
    }

	@Override
	public String toString() {
		return getWorkflowType().toString();
	}

}
