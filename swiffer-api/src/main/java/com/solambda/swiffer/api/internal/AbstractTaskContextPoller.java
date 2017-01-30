package com.solambda.swiffer.api.internal;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.exceptions.TaskContextPollingException;

public abstract class AbstractTaskContextPoller<T extends TaskContext> implements TaskContextPoller<T> {

	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

	protected AmazonSimpleWorkflow swf;
	protected String domain;
	protected String taskList;
	protected String identity;
	private Future<T> currentPollingOperation;
	private ExecutorService executor;

	public AbstractTaskContextPoller(final AmazonSimpleWorkflow swf, final String domain, final String taskList,
			final String identity) {
		super();
		this.swf = Preconditions.checkNotNull(swf, "please specify a SWF client!");
		this.domain = Preconditions.checkNotNull(domain, "please specify the domain!");
		this.taskList = Preconditions.checkNotNull(taskList, "please specify the task list to poll!");
		this.identity = identity;
		this.executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public T poll() throws TaskContextPollingException {
		if (this.currentPollingOperation != null) {
			throw new IllegalStateException("pending polling operation!");
		}
		try {
			this.currentPollingOperation = this.executor.submit(() -> pollForTask());
			return this.currentPollingOperation.get(70, TimeUnit.SECONDS);
		} catch (final CancellationException e1) {
			// was requested to stop
			this.LOGGER.info("Cancelling the polling operation, the poller was requested to stop.");
			return null;
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
		// When we stop, we can only ask the executor not to accept new polling
		// tasks.
		// we cannot stop the underlying swf polling operation (it's gonna block
		// for 60sec)
		// even with our previous implem which called
		// "currentPollingOperation.cancel(true);"
		// on the contraty we should return a task if it is available
		this.executor.shutdown();
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