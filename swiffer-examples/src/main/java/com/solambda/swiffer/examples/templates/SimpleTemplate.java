package com.solambda.swiffer.examples.templates;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.OnActivityCompleted;
import com.solambda.swiffer.api.OnActivityFailed;
import com.solambda.swiffer.api.OnSignalReceived;
import com.solambda.swiffer.api.OnTimerFired;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.examples.ActivityDefinitions.ParseInteger;
import com.solambda.swiffer.examples.WorkflowDefinitions.SimpleExampleWorkflowDefinition;

@SimpleExampleWorkflowDefinition
public class SimpleTemplate {

	@OnWorkflowStarted
	public void onStart(final String stringToParse, final Decisions decideTo) {
		decideTo.scheduleActivityTask(ParseInteger.class, stringToParse);
	}

	@OnActivityCompleted(activity = ParseInteger.class)
	// NOTE: this is stupid: the output should be an Integer, but the library
	// does not know how to deserialize yet !
	public void onParseInteger(final String output, @Input final String input, final Decisions decideTo) {
		final String workflowResult = String.format("String '%s' parsed to the integer %s", input, output);
		decideTo.completeWorfklow(workflowResult);
	}

	@OnActivityFailed(activity = ParseInteger.class)
	public void couldNotParseInteger(final String reason) {

	}

	@OnTimerFired(timerId = "")
	public void timerFired() {

	}

	@OnSignalReceived(signalName = "")
	public void signalReceived() {

	}

}
