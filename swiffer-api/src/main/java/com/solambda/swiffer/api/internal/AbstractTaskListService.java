package com.solambda.swiffer.api.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.solambda.swiffer.api.TaskListService;
import com.solambda.swiffer.api.exceptions.TaskContextPollingException;

public abstract class AbstractTaskListService<T extends TaskContext> implements TaskListService {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	protected TaskContextPoller<T> poller;
	private AbstractExecutionThreadService daemonService;

	public AbstractTaskListService(final TaskContextPoller<T> poller) {
		super();
		this.poller = poller;
	}

	@Override
	public void start() {
		if (this.daemonService == null) {
			this.daemonService = new AbstractExecutionThreadService() {
				@Override
				protected void run() throws Exception {
					while (isRunning()) {
						try {
							final T task = pollTaskList();
							if (task != null) {
								executeTask(task);
							}
						} catch (final Exception e) {
							AbstractTaskListService.this.LOGGER.error(
									"Error running poller. Service is going to stop now.",
									e);
							throw e;
						}
					}
				}

				@Override
				protected void triggerShutdown() {
					super.triggerShutdown();
					AbstractTaskListService.this.poller.stop();
				}
			};
			this.daemonService.addListener(new Service.Listener() {
				@Override
				public void failed(final State from, final Throwable failure) {
					super.failed(from, failure);
				}
			}, MoreExecutors.directExecutor());
		}
		final State state = this.daemonService.state();
		switch (state) {
		case NEW:
			this.daemonService.startAsync();
			this.daemonService.awaitRunning();
			break;
		case STARTING:
			this.daemonService.awaitRunning();
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

	private T pollTaskList() throws TaskContextPollingException {
		final T t = this.poller.poll();
		return t;
	}

	/**
	 * A test only method that poll the task list once and execute the task
	 *
	 * @throws TaskContextPollingException
	 */
	public void pollAndExecuteTask() throws TaskContextPollingException {
		final T task = pollTaskList();
		if (task == null) {
			throw new IllegalStateException("no task available for execution!");
		}
		executeTaskImmediately(task);
	}

	@Override
	public void stop() {
		if (this.daemonService == null) {

		} else {
			this.daemonService.stopAsync();
			this.daemonService = null;
		}
	}

	@Override
	public boolean isStarted() {
		return this.daemonService != null
				&& (this.daemonService.isRunning() || this.daemonService.state() == State.STARTING);
	}

}
