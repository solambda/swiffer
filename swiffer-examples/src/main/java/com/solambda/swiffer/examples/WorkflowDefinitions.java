package com.solambda.swiffer.examples;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.solambda.swiffer.api.WorkflowType;

public class WorkflowDefinitions {

	public static final String SIGNAL_NAME = "signal1";

	@WorkflowType(name = "SimpleExampleWorkflow", version = "2", defaultExecutionStartToCloseTimeout = 10)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface SimpleExampleWorkflowDefinition {

	}
}
