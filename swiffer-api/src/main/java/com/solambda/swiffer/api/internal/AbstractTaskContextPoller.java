package com.solambda.swiffer.api.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.exceptions.TaskContextPollingException;

public abstract class AbstractTaskContextPoller<T extends TaskContext> implements TaskContextPoller<T> {

	protected AmazonSimpleWorkflow swf;
	protected String domain;
	protected String taskList;
	protected String identity;
	private Future<T> currentPollingOperation;

	public AbstractTaskContextPoller(final AmazonSimpleWorkflow swf, final String domain, final String taskList,
			final String identity) {
		super();
		this.swf = Preconditions.checkNotNull(swf, "please specify a SWF client!");
		this.domain = Preconditions.checkNotNull(domain, "please specify the domain!");
		this.taskList = Preconditions.checkNotNull(taskList, "please specify the task list to poll!");
		this.identity = identity;
	}

	@Override
	public T poll() throws TaskContextPollingException {
		if (this.currentPollingOperation != null) {
			throw new IllegalStateException("pending polling operation!");
		}
		try {
			this.currentPollingOperation = Executors.newSingleThreadExecutor().submit(() -> pollForTask());
			return this.currentPollingOperation.get(70, TimeUnit.SECONDS);
		} catch (final InterruptedException e1) {
			throw new RuntimeException(e1);
		} catch (final ExecutionException e1) {
			final String message = String.format("[%s:%s] Cannot poll tasklist %s",
					this.domain, this.identity, this.taskList);
			throw new TaskContextPollingException(message, e1.getCause());
		} catch (final TimeoutException e) {
			throw new IllegalStateException(
					String.format("[%s:%s] Polling tasklist %s error ! Should never timeout after 70 seconds !",
							this.domain, this.identity, this.taskList),
					e);
		} finally {
			this.currentPollingOperation = null;
		}
	}

	protected abstract T pollForTask() throws Exception;

	@Override
	public void stop() {
		if (this.currentPollingOperation != null) {
			this.currentPollingOperation.cancel(true);
		}
	}

	@Override
	public AmazonSimpleWorkflow swf() {
		return this.swf;
	}

	@Override
	public String domain() {
		return this.domain;
	}

}