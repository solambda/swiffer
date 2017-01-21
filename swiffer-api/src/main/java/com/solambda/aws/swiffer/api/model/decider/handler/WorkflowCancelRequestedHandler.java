package com.solambda.aws.swiffer.api.model.decider.handler;

import com.solambda.aws.swiffer.api.model.decider.Decisions;
import com.solambda.aws.swiffer.api.model.decider.context.WorkflowCancelRequestedContext;

public interface WorkflowCancelRequestedHandler extends EventContextHandler<WorkflowCancelRequestedContext> {
	public void onWorkflowCancelRequested(WorkflowCancelRequestedContext context, Decisions decisions);
}
