package com.solambda.swiffer.api.model.decider;

import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.model.WorkflowTypeId;

public abstract class DeciderConfigurerImpl extends EventContextHandlerRegistryBuilder implements DeciderConfigurer {

	private EventContextHandlerRegistryBuilder builder;

	public DeciderConfigurerImpl(final WorkflowTypeId workflowType) {
		super(workflowType);
	}

	@Override
	public EventContextHandlerRegistry registry() {
		// FIXME
		// if (registry == null) {
		// Preconditions.checkState(workflowType != null, "workflow type cannot
		// be null!");
		// builder = new EventContextHandlerRegistryBuilder(workflowType);
		// configure();
		// registry = builder.build();
		// }
		// return registry;
		return null;
	}

	@Override
	public WorkflowExecutionHandlerRegistryBuilder onWorkflow() {
		return builder.onWorkflow();
	}

	@Override
	public TaskHandlerRegistryBuilder on(final VersionedName type) {
		return builder.on(type);
	}

	protected abstract void configure();
}
