package com.solambda.aws.swiffer.api.model.decider;

import com.solambda.aws.swiffer.api.model.TaskType;
import com.solambda.aws.swiffer.api.model.WorkflowTypeId;
import com.google.common.base.Preconditions;

public abstract class DeciderConfigurerImpl extends EventContextHandlerRegistryBuilder implements DeciderConfigurer {

	private EventContextHandlerRegistryBuilder builder;

	public DeciderConfigurerImpl(final WorkflowTypeId workflowType) {
		super(workflowType);
	}

	@Override
	public EventContextHandlerRegistry registry() {
		if (registry == null) {
			Preconditions.checkState(workflowType != null, "workflow type cannot be null!");
			builder = new EventContextHandlerRegistryBuilder(workflowType);
			configure();
			registry = builder.build();
		}
		return registry;
	}

	@Override
	public WorkflowExecutionHandlerRegistryBuilder onWorkflow() {
		return builder.onWorkflow();
	}

	@Override
	public TaskHandlerRegistryBuilder on(final TaskType type) {
		return builder.on(type);
	}

	protected abstract void configure();
}
