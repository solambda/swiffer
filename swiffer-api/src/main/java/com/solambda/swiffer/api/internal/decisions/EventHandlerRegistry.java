package com.solambda.swiffer.api.internal.decisions;

import java.util.Map;

import com.solambda.swiffer.api.retry.RetryControl;

public class EventHandlerRegistry {

	private final Map<EventHandlerType, EventHandler> eventHandlerRegistry;

	private EventHandler defaultFailedActivityHandler;
    private EventHandler defaultTimedOutActivityHandler;
    private EventHandler defaultRetryTimerFiredHandler;

	public EventHandlerRegistry(final Map<EventHandlerType, EventHandler> eventHandlerRegistry) {
		super();
		this.eventHandlerRegistry = eventHandlerRegistry;
	}

	public EventHandler get(final EventHandlerType key) {
		return this.eventHandlerRegistry.get(key);
	}

    EventHandler getDefaultFailedActivityHandler() {
        return defaultFailedActivityHandler;
    }

    void setDefaultFailedActivityHandler(EventHandler defaultFailedActivityHandler) {
        this.defaultFailedActivityHandler = defaultFailedActivityHandler;
    }

    EventHandler getDefaultTimedOutActivityHandler() {
        return defaultTimedOutActivityHandler;
    }

    void setDefaultTimedOutActivityHandler(EventHandler defaultTimedOutActivityHandler) {
        this.defaultTimedOutActivityHandler = defaultTimedOutActivityHandler;
    }

    EventHandler getDefaultRetryTimerFiredHandler(String timerId) {
        if (timerId.startsWith(RetryControl.RETRY_TIMER)) {
            return defaultRetryTimerFiredHandler;
        }
        return null;
    }

    void setDefaultRetryTimerFiredHandler(EventHandler defaultRetryTimerFiredHandler) {
        this.defaultRetryTimerFiredHandler = defaultRetryTimerFiredHandler;
    }
}
