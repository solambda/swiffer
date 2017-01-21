package com.solambda.aws.swiffer.api.model.decider.handler;

import com.solambda.aws.swiffer.api.model.decider.context.WorkflowTerminatedContext;

public interface WorkflowTerminatedHandler extends EventContextHandler<WorkflowTerminatedContext> {
	public void onWorkflowTerminated(WorkflowTerminatedContext context);
}
