package com.solambda.swiffer.api.internal.decisions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.model.CancelWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.ContinueAsNewWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionFailedCause;
import com.solambda.swiffer.api.retry.RetryControl;

public class EventHandlerRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandlerRegistry.class);

	private final Map<EventHandlerType, EventHandler> eventHandlerRegistry;

	private EventHandler defaultFailedActivityHandler;
    private EventHandler defaultTimedOutActivityHandler;
    private EventHandler defaultRetryTimerFiredHandler;
    private EventHandler defaultCompleteWorkflowExecutionFailedHandler;
    private EventHandler defaultCancelWorkflowExecutionFailedHandler;
    private EventHandler defaultFailWorkflowExecutionFailedHandler;
    private EventHandler defaultContinueAsNewWorkflowExecutionFailedHandler;

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

    EventHandler getDefaultCompleteWorkflowExecutionFailedHandler(String cause) {
        try {
            CompleteWorkflowExecutionFailedCause completeFailedCause = CompleteWorkflowExecutionFailedCause.fromValue(cause);
            if (completeFailedCause == CompleteWorkflowExecutionFailedCause.UNHANDLED_DECISION) {
                return defaultCompleteWorkflowExecutionFailedHandler;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to create defaultCompleteWorkflowExecutionFailedHandler.", e);
        }

        return null;
    }

    void setDefaultCompleteWorkflowExecutionFailedHandler(EventHandler defaultCompleteWorkflowExecutionFailedHandler) {
        this.defaultCompleteWorkflowExecutionFailedHandler = defaultCompleteWorkflowExecutionFailedHandler;
    }

    EventHandler getDefaultCancelWorkflowExecutionFailedHandler(String cause) {
        try {
            CancelWorkflowExecutionFailedCause cancelFailedCause = CancelWorkflowExecutionFailedCause.fromValue(cause);
            if (cancelFailedCause == CancelWorkflowExecutionFailedCause.UNHANDLED_DECISION) {
                return defaultCancelWorkflowExecutionFailedHandler;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to create defaultCancelWorkflowExecutionFailedHandler.", e);
        }

        return null;
    }

    void setDefaultCancelWorkflowExecutionFailedHandler(EventHandler defaultCancelWorkflowExecutionFailedHandler) {
        this.defaultCancelWorkflowExecutionFailedHandler = defaultCancelWorkflowExecutionFailedHandler;
    }

    EventHandler getDefaultFailWorkflowExecutionFailedHandler(String cause) {
        try {
            FailWorkflowExecutionFailedCause failFailedCause = FailWorkflowExecutionFailedCause.fromValue(cause);
            if (failFailedCause == FailWorkflowExecutionFailedCause.UNHANDLED_DECISION) {
                return defaultFailWorkflowExecutionFailedHandler;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to create defaultFailWorkflowExecutionFailedHandler.", e);
        }

        return null;
    }

    void setDefaultFailWorkflowExecutionFailedHandler(EventHandler defaultFailWorkflowExecutionFailedHandler) {
        this.defaultFailWorkflowExecutionFailedHandler = defaultFailWorkflowExecutionFailedHandler;
    }

    EventHandler getDefaultContinueAsNewWorkflowExecutionFailedHandler(String cause) {
        try {
            ContinueAsNewWorkflowExecutionFailedCause continueAsNewFailedCause = ContinueAsNewWorkflowExecutionFailedCause.fromValue(cause);
            if (continueAsNewFailedCause == ContinueAsNewWorkflowExecutionFailedCause.UNHANDLED_DECISION) {
                return defaultContinueAsNewWorkflowExecutionFailedHandler;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to create defaultContinueAsNewWorkflowExecutionFailedHandler.", e);
        }

        return null;
    }

    void setDefaultContinueAsNewWorkflowExecutionFailedHandler(EventHandler defaultContinueAsNewWorkflowExecutionFailedHandler) {
        this.defaultContinueAsNewWorkflowExecutionFailedHandler = defaultContinueAsNewWorkflowExecutionFailedHandler;
    }
}
