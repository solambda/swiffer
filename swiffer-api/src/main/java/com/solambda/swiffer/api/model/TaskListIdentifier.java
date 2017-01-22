package com.solambda.swiffer.api.model;

import com.solambda.swiffer.api.internal.utils.SWFUtils;

public class TaskListIdentifier {

	private String name;

	public TaskListIdentifier(final String name) {
		super();
		this.name = SWFUtils.checkId(name);
	}

	public String getName() {
		return this.name;
	}
}
