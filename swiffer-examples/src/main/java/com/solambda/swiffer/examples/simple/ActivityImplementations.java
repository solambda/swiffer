package com.solambda.swiffer.examples.simple;

import com.solambda.swiffer.api.Executor;
import com.solambda.swiffer.examples.simple.ActivityDefinitions.ParseInteger;

public class ActivityImplementations {

	@Executor(activity = ParseInteger.class)
	public Integer parseInteger(final String input) {
		return Integer.parseInt(input);
	}

}
