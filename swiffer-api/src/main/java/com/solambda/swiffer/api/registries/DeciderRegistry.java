package com.solambda.swiffer.api.registries;

import java.util.HashMap;
import java.util.Map;

import com.solambda.swiffer.api.model.WorkflowTypeId;
import com.solambda.swiffer.api.model.decider.Decider;

public class DeciderRegistry {

	private Map<WorkflowTypeId, Decider> deciders = new HashMap<>();

	public Decider getDecider(final WorkflowTypeId identifier) {
		return deciders.get(identifier);
	}

	public DeciderRegistry register(final WorkflowTypeId identifier, final Decider decider) {
		deciders.put(identifier, decider);
		return this;
	}
}
