package com.solambda.swiffer.api.internal;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service.State;
import com.solambda.swiffer.api.TaskListPoller;

public abstract class AbstractTaskListPoller<T> implements TaskListPoller {

	protected ContextProvider<T> provider;
	private AbstractExecutionThreadService pollingService;

	public AbstractTaskListPoller(final ContextProvider<T> provider) {
		super();
		this.provider = provider;
	}

	@Override
	public void start() {
		if (this.pollingService == null) {
			this.pollingService = new AbstractExecutionThreadService() {
				@Override
				protected void run() throws Exception {
					while (isRunning()) {
						final T task = pollTaskList();
						executeTask(task);
					}
				}
			};
		}
		final State state = this.pollingService.state();
		switch (state) {
		case NEW:
			this.pollingService.startAsync();
			this.pollingService.awaitRunning();
			break;
		case STARTING:
			this.pollingService.awaitRunning();
			break;
		case RUNNING:
			break;
		case STOPPING:
		case FAILED:
		case TERMINATED:
		default:
			throw new IllegalStateException("polling service is " + state + "!");
		}
	}

	/**
	 * Execute the task.
	 *
	 * @param task
	 */
	protected abstract void executeTask(final T task);

	/**
	 * Execute the task. For testing purpose
	 *
	 * @param task
	 */
	protected abstract void executeTaskImmediately(final T task);

	private T pollTaskList() {
		final T t = this.provider.get();
		return t;
	}

	/**
	 * A test only method that poll the task list once and execute the task
	 */
	public void pollAndExecuteTask() {
		final T task = pollTaskList();
		if (task == null) {
			throw new IllegalStateException("no task available for execution!");
		}
		executeTaskImmediately(task);
	}

	@Override
	public void stop() {
		if (this.pollingService == null) {

		} else {
			this.pollingService.stopAsync();
			this.pollingService = null;
		}
	}

	@Override
	public boolean isStarted() {
		return this.pollingService != null
				&& (this.pollingService.isRunning() || this.pollingService.state() == State.STARTING);
	}

}
