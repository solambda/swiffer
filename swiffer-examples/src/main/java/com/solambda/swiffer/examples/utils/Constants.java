package com.solambda.swiffer.examples.utils;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;

public class Constants {

	public static final String DOMAIN = "domainForTestingSwiffer";
	// public static final WorkflowTypeId WORKFLOW = new WorkflowTypeId(new
	// DomainIdentifier(DOMAIN), "swiffer-workflow-test", "1.0.0");

	public static AmazonSimpleWorkflow swf() {
		return new AmazonSimpleWorkflowClient(new DefaultAWSCredentialsProviderChain());
	}

}
