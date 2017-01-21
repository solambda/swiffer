package com.solambda.swiffer.api.model.decider.handler;

import com.solambda.swiffer.api.model.decider.Decisions;
import com.solambda.swiffer.api.model.decider.context.WorkflowStartedContext;

@FunctionalInterface
public interface WorkflowStartedHandler extends EventContextHandler<WorkflowStartedContext> {
	public void onWorkflowStarted(WorkflowStartedContext context, Decisions decisions);
}
