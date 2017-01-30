package com.solambda.swiffer.examples.templates;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.OnActivityCompleted;
import com.solambda.swiffer.api.OnSignalReceived;
import com.solambda.swiffer.api.OnTimerFired;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.examples.ActivityDefinitions.ParseInteger;
import com.solambda.swiffer.examples.WorkflowDefinitions;
import com.solambda.swiffer.examples.WorkflowDefinitions.SimpleExampleWorkflowDefinition;

@SimpleExampleWorkflowDefinition
public class SimpleTemplate {

	private static final String TIMER_ID = "timer1";
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTemplate.class);

	@OnWorkflowStarted
	public void onStart(final String stringToParse, final Decisions decideTo) {
		decideTo.scheduleActivityTask(ParseInteger.class, stringToParse);
	}

	@OnActivityCompleted(activity = ParseInteger.class)
	// NOTE: this is stupid: the output should be an Integer, but the library
	// does not know how to deserialize yet !
	public void onParseInteger(final String output, @Input final String input, final Decisions decideTo) {
		final String workflowResult = String.format("String '%s' parsed to the integer %s", input, output);
		LOGGER.info("Task correctly executed with result {}", output);
		decideTo.startTimer(TIMER_ID, Duration.ofSeconds(3), input);

	}

	@OnTimerFired(timerId = TIMER_ID)
	public void timerFired(final String control, final Decisions decideTo) {
		LOGGER.info("Timer fired with control {}", control);
		decideTo.scheduleActivityTask(ParseInteger.class, control);
	}

	@OnSignalReceived(signalName = WorkflowDefinitions.SIGNAL_NAME)
	public void signalReceived(final String input, final Decisions decideTo) {
		decideTo.cancelTimer(TIMER_ID);
		LOGGER.info("Signal received with input  {}", input);
		decideTo.completeWorfklow(input);
	}

}
