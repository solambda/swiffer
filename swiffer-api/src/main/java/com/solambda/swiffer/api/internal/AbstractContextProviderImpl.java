package com.solambda.swiffer.api.internal;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowAsync;

public class AbstractContextProviderImpl {

	protected AmazonSimpleWorkflow swf;
	protected String domain;
	protected String taskList;
	protected String identity;

	public AbstractContextProviderImpl(final AmazonSimpleWorkflow swf, final String domain, final String taskList, final String identity) {
		super();
		this.swf = swf;
		this.domain = domain;
		this.taskList = taskList == null ? "default" : taskList;
		this.identity = identity;
	}

}