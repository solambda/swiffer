package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.decisions.EventContext;

public interface ExternalWorkflowContext extends EventContext {

    String getExternalWorkflowId();

    String getExternalWorkflowRunId();
}
