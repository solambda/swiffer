package com.solambda.swiffer.api;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;

public class Swiffer {

	private AmazonSimpleWorkflow swf;
	private String domain;

	public Swiffer(final AmazonSimpleWorkflow swf, final String domain) {
		super();
		this.swf = swf;
		this.domain = domain;
	}

	public static Swiffer get(final AmazonSimpleWorkflow swf, final String domain) {
		return new Swiffer(swf, domain);
	}

	public WorkerBuilder newWorkerBuilder() {
		return new WorkerBuilder();
	}

	public DeciderBuilder newDeciderBuilder() {
		return new DeciderBuilder();
	}

	/**
	 * Starts an execution of the workflow type in the specified domain using
	 * the provided workflowId and input data.
	 *
	 * @param workflowTypeDefinition
	 * @param workflowId
	 * @return the runId of this workflow execution
	 */
	public String startWorkflow(final Class<?> workflowTypeDefinition, final String workflowId) {

	}

}
