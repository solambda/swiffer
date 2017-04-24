package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.context.identifier.WorkflowName;
import com.solambda.swiffer.api.internal.decisions.EventContext;

public interface ChildWorkflowContext extends EventContext {

    WorkflowName childWorkflowName();

    String childWorkflowId();

    String childWorkflowRunId();
}
