package com.solambda.swiffer.api.internal;

import java.util.Map;

import com.solambda.swiffer.api.internal.activities.ActivityExecutor;

public class ActivityExecutorRegistry {

	private final Map<VersionedName, ActivityExecutor> registry;

	public ActivityExecutorRegistry(final Map<VersionedName, ActivityExecutor> registry) {
		super();
		this.registry = registry;
	}

	public ActivityExecutor get(final VersionedName activityType) {
		return this.registry.get(activityType);
	}

}
