package com.solambda.swiffer.api.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.swiffer.api.Worker;
import com.solambda.swiffer.api.exceptions.ActivityTaskExecutionFailedException;
import com.solambda.swiffer.api.internal.activities.ActivityExecutionReporter;
import com.solambda.swiffer.api.internal.activities.ActivityExecutor;
import com.solambda.swiffer.api.internal.activities.ActivityExecutorRegistry;
import com.solambda.swiffer.api.internal.activities.ActivityTaskContext;

public class WorkerImpl extends AbstractTaskListService<ActivityTaskContext> implements Worker {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkerImpl.class);
	private ExecutorService executor;

	private ActivityExecutorRegistry registry;

	final ActivityExecutionReporter reporter;

	public WorkerImpl(
			final TaskContextPoller<ActivityTaskContext> poller,
			final ActivityExecutorRegistry registry,
			final ActivityExecutionReporter reporter) {
		super(poller);
		this.executor = Executors.newFixedThreadPool(10);
		this.registry = registry;
		this.reporter = reporter;
	}

	@Override
	protected void executeTask(final ActivityTaskContext task) {
		// emit the task execution in another thread.
		this.executor.submit(() -> {
			executeTaskImmediately(task);
		});
	}

	@Override
	protected void executeTaskImmediately(final ActivityTaskContext task) {

		// retrieve the executor
		final VersionedName activityType = task.activityType();
		final ActivityExecutor executor = getActivityExecutor(activityType);
		if (executor == null) {
			final String reason = String.format("no executor defined "
					+ "for activity {name=\"%s\",version=\"%s\"", activityType.name(), activityType.version());
			LOGGER.error(reason);
			this.reporter.failed(task.taskToken(), new Failure(reason));
		} else {
			execute(task, executor, this.reporter);
		}
	}

	private ActivityExecutor getActivityExecutor(final VersionedName activityType) {
		return this.registry.get(activityType);
	}

	private void execute(final ActivityTaskContext context, final ActivityExecutor executor,
			final ActivityExecutionReporter reporter) {
		try {
			final String output = executor.execute(context);
			reporter.completed(context.taskToken(), output);
		} catch (final ActivityTaskExecutionFailedException ex) {
			LOGGER.error("Activity execution failed '{}', v='{}'", context.activityType().name(),
					context.activityType().version(), ex);
			final StringWriter errors = new StringWriter();
			ex.printStackTrace(new PrintWriter(errors));
			final String details = errors.toString();
			reporter.failed(context.taskToken(), Failure.reason("Task execution failed").details(details));
		} catch (final Exception exception) {
			LOGGER.error("Exception during activity execution '{}', v='{}'", context.activityType().name(),
					context.activityType().version(), exception);
			final StringWriter errors = new StringWriter();
			exception.printStackTrace(new PrintWriter(errors));
			final String details = errors.toString();
			reporter.failed(context.taskToken(), Failure.reason("Task execution failed").details(details));
		}
	}

	@Override
	public void stop() {
		// super.stop blocks until the service poll and execute the last task
		super.stop();
		// ... so that we can safely shutdown
		this.executor.shutdown();
		try {
			this.executor.awaitTermination(1, TimeUnit.HOURS);
		} catch (final InterruptedException e) {
			throw new IllegalStateException("Awaited more than 1 hours for an activity to terminate!");
		}
	}

}
