package com.solambda.swiffer.examples;

import com.solambda.swiffer.api.ActivityType;

public class ActivityDefinitions {

	@ActivityType(name = "ParseInteger", version = "1")
	public static interface ParseInteger {
	}

	@ActivityType(name = "FailingActivity", version = "1")
	public @interface FailingActivity {
	}
}
