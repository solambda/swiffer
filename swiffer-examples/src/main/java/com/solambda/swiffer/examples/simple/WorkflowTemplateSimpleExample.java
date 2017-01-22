package com.solambda.swiffer.examples.simple;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.OnActivityCompleted;
import com.solambda.swiffer.api.OnActivityFailed;
import com.solambda.swiffer.api.OnSignalReceived;
import com.solambda.swiffer.api.OnTimerFired;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.examples.simple.ActivityDefinitions.ParseInteger;
import com.solambda.swiffer.examples.simple.WorkflowDefinitions.SimpleExampleWorkflowDefinition;

@SimpleExampleWorkflowDefinition
public class WorkflowTemplateSimpleExample {

	@OnWorkflowStarted
	public void onStart(final String stringToParse, final Decisions decideTo) {
		decideTo.scheduleActivityTask(ParseInteger.class, stringToParse);
	}

	@OnActivityCompleted(activity = ParseInteger.class)
	public void onParseInteger(final Integer output, @Input final String input, final Decisions decideTo) {
		final String workflowResult = String.format("String '%s' parsed to the integer %s", input, output);
		decideTo.completeWorfklow(workflowResult);
	}

	@OnActivityFailed(activity = ParseInteger.class)
	public void couldNotParseInteger(final String reason, final String details) {

	}

	@OnTimerFired(timerId = "")
	public void timerFired() {

	}

	@OnSignalReceived(signalName = "")
	public void signalReceived() {

	}

}
