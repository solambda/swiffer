package com.solambda.swiffer.api.model.tasks.impl;

import com.solambda.swiffer.api.model.tasks.TaskInvoker;

public class TaskInvokerImpl implements TaskInvoker {

	// the object in which task methods are defined,
	// or null if the method is static
	private Object taskObject;

	public TaskInvokerImpl(final Object taskObject, final Object object) {
		this.taskObject = taskObject;
	}

	@Override
	public String invoke(final String input) throws Exception {
		return null;
	}

}
