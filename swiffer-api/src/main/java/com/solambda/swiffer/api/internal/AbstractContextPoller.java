package com.solambda.swiffer.api.internal;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;

public abstract class AbstractContextPoller<T extends TaskContext> implements TaskContextPoller<T> {

	protected AmazonSimpleWorkflow swf;
	protected String domain;
	protected String taskList;
	protected String identity;

	public AbstractContextPoller(final AmazonSimpleWorkflow swf, final String domain, final String taskList,
			final String identity) {
		super();
		this.swf = swf;
		this.domain = domain;
		this.taskList = taskList == null ? "default" : taskList;
		this.identity = identity;
	}

	@Override
	public T poll() {

		try {
			// emit the polling in a asynchronous thread
			return pollForTask();
		} catch (final Exception e) {
			throw new IllegalStateException(String.format("[%s:%s] Cannot poll tasklist for task %s",
					this.domain, this.identity, this.taskList), e);
		}
	}

	protected abstract T pollForTask() throws Exception;

	@Override
	public void stop() {
		// FIXME: should stop an executor service that emitted a runnable that
		// polls
	}

}