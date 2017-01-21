package com.solambda.aws.swiffer.api.model.decider;

import com.solambda.aws.swiffer.api.model.WorkflowTypeId;

public interface DeciderConfigurer {

	public WorkflowTypeId workflowType();

	public EventContextHandlerRegistry registry();
}
