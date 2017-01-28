package com.solambda.swiffer.api.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.solambda.swiffer.api.Worker;
import com.solambda.swiffer.api.internal.activities.ActivityExecutionReporter;
import com.solambda.swiffer.api.internal.activities.ActivityExecutor;
import com.solambda.swiffer.api.internal.activities.ActivityExecutorRegistry;
import com.solambda.swiffer.api.internal.activities.ActivityTaskContext;
import com.solambda.swiffer.api.internal.activities.ActivityTaskContextProvider;
import com.solambda.swiffer.api.internal.activities.exceptions.ActivityTaskExecutionFailedException;

public class WorkerImpl extends AbstractTaskListPoller<ActivityTaskContext> implements Worker {

	private ExecutorService executor;

	private ActivityExecutorRegistry registry;

	final ActivityExecutionReporter reporter;

	public WorkerImpl(
			final ActivityTaskContextProvider provider,
			final ActivityExecutorRegistry registry,
			final ActivityExecutionReporter reporter) {
		super(provider);
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
			final String reason = String.format("no activity executor defined "
					+ "for activity type {name=\"%s\",version=\"%s\"", activityType.name(), activityType.version());
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
			final StringWriter errors = new StringWriter();
			ex.printStackTrace(new PrintWriter(errors));
			final String details = errors.toString();
			reporter.failed(context.taskToken(), Failure.reason("Task execution failed").details(details));
		} catch (final Exception exception) {
			final StringWriter errors = new StringWriter();
			exception.printStackTrace(new PrintWriter(errors));
			final String details = errors.toString();
			reporter.failed(context.taskToken(), Failure.reason("Task execution failed").details(details));
		}
	}
}
