package com.solambda.swiffer.examples;

import com.solambda.swiffer.api.Executor;
import com.solambda.swiffer.examples.ActivityDefinitions.FailingActivity;
import com.solambda.swiffer.examples.ActivityDefinitions.ParseInteger;

public class ActivityImplementations {

	@Executor(activity = ParseInteger.class)
	public Integer parseInteger(final String input) {
		return Integer.parseInt(input);
	}

	@Executor(activity = FailingActivity.class)
	public void failingActivity(String input){
		throw new RuntimeException("Failing Activity");
	}
}
