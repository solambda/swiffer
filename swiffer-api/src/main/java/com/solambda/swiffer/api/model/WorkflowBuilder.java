package com.solambda.swiffer.api.model;

import com.solambda.swiffer.api.registries.WorkflowRegistry;

public class WorkflowBuilder extends AbstractSwfObject<WorkflowBuilder> {

	private WorkflowTypeId workflowTypeId;
	private String workflowId;

	public WorkflowBuilder() {
		super();
	}

	public WorkflowBuilder type(final WorkflowTypeId type) {
		this.workflowTypeId = type;
		return this;
	}

	public WorkflowBuilder id(final String workflowId) {
		this.workflowId = workflowId;
		return this;
	}

	public Workflow build() {
		checkWorkflowIsRegistered();
		return new WorkflowImpl(swf, domain, workflowTypeId.name(), workflowTypeId.version(), workflowId);
	}

	private void checkWorkflowIsRegistered() {
		WorkflowRegistry registry = new WorkflowRegistry(swf);
		if (!registry.exists(workflowTypeId)) {
			throw new IllegalStateException("the workflow " + workflowTypeId + " is not registered in the domain " + domain);
		}
	}
}
