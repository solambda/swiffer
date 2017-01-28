package com.solambda.swiffer.api.internal.decisions;

import java.util.Map;

import com.solambda.swiffer.api.WorkflowType;
import com.solambda.swiffer.api.internal.VersionedName;

/**
 * Store workflow templates for each {@link WorkflowType}.
 * <p>
 */
public class WorkflowTemplateRegistry {

	private final Map<VersionedName, WorkflowTemplate> registry;

	public WorkflowTemplateRegistry(final Map<VersionedName, WorkflowTemplate> registry) {
		super();
		this.registry = registry;
	}

	public WorkflowTemplate get(final VersionedName workflowType) {
		return this.registry.get(workflowType);
	}
}
