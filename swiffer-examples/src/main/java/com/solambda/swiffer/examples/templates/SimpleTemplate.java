package com.solambda.swiffer.examples.templates;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.OnActivityCompleted;
import com.solambda.swiffer.api.OnMarkerRecorded;
import com.solambda.swiffer.api.OnSignalReceived;
import com.solambda.swiffer.api.OnTimerFired;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.api.Output;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskContext;
import com.solambda.swiffer.examples.ActivityDefinitions.ParseInteger;
import com.solambda.swiffer.examples.WorkflowDefinitions;
import com.solambda.swiffer.examples.WorkflowDefinitions.SimpleExampleWorkflowDefinition;

@SimpleExampleWorkflowDefinition
public class SimpleTemplate {

	private static final String TIMER_ID = "timer1";
    private static final String MARKER_NAME = "timer-time-marker";
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTemplate.class);

	@OnWorkflowStarted
	public void onStart(final String stringToParse, final Decisions decideTo) {
		decideTo.startTimer(TIMER_ID, Duration.ofSeconds(3), stringToParse);
	}

	@OnActivityCompleted(ParseInteger.class)
	public void onParseInteger(@Output Integer output, @Input final String input, DecisionTaskContext context, final Decisions decideTo) {
		LOGGER.info("Task correctly executed with result {}", output);
        LOGGER.info("Get recorded marker details {}", context.getMarkerDetails(MARKER_NAME, LocalDateTime.class));
    }

	@OnTimerFired(TIMER_ID)
	public void timerFired(final String control, final Decisions decideTo) {
		LOGGER.info("Timer fired with control {}", control);
        LocalDateTime dateTime = LocalDateTime.now();
        LOGGER.info("Record Marker with details {}", dateTime);

		decideTo.scheduleActivityTask(ParseInteger.class, control)
                .recordMarker(MARKER_NAME, dateTime);
	}

	@OnSignalReceived(WorkflowDefinitions.SIGNAL_NAME)
	public void signalReceived(final String input, final Decisions decideTo) {
		LOGGER.info("Signal received with input  {}", input);
		decideTo.completeWorkflow(input);
	}

	@OnMarkerRecorded(MARKER_NAME)
    public void onMarkerRecorded(@Input LocalDateTime input) {
        LOGGER.info("Marker Recorded with input {}", input);
    }
}
