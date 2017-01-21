package com.solambda.swiffer.api.model.decider;

import com.solambda.swiffer.api.model.WorkflowTypeId;

public interface DeciderConfigurer {

	public WorkflowTypeId workflowType();

	public EventContextHandlerRegistry registry();
}
