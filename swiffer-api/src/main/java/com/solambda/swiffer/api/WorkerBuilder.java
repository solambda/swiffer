package com.solambda.swiffer.api;

import java.util.Arrays;
import java.util.List;

import com.solambda.swiffer.api.internal.WorkerImpl;

public class WorkerBuilder {
	private String taskList;
	private String identity;
	private List<Object> executors;

	public Worker build() {
		// do the introspection of the executor
		// register the activity types
		// create the worker registry (
		return new WorkerImpl();
	}

	/**
	 * @param taskList
	 * @return
	 */
	public WorkerBuilder taskList(final String taskList) {
		this.taskList = taskList;
		return this;
	}

	public WorkerBuilder identity(final String identity) {
		this.identity = identity;
		return this;
	}

	public WorkerBuilder executors(final Object... executors) {
		this.executors = Arrays.asList(executors);
		return this;
	}
}
