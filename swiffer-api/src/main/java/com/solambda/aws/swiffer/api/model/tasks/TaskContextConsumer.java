package com.solambda.aws.swiffer.api.model.tasks;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.aws.swiffer.api.model.decider.ContextProvider;
import com.solambda.aws.swiffer.api.model.decider.impl.ContextProviderThreadedImpl;
import com.solambda.aws.swiffer.api.model.tasks.impl.TaskPoller;
import com.solambda.aws.swiffer.api.model.tasks.impl.TaskReportImpl;

public class TaskContextConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskPoller.class);

	private ContextProvider<TaskContext> provider;
	private TaskExecutor executor;
	private TaskReport reporter;
	private ExecutorService service;

	private AmazonSimpleWorkflow client;

	public TaskContextConsumer(
			final AmazonSimpleWorkflow swf,
			final String domain,
			final String taskList,
			final String identity,
			final ExecutorService service,
			final TaskExecutor executor) {
		this(swf, domain, taskList, identity, service, executor, null);
	}

	public TaskContextConsumer(
			final AmazonSimpleWorkflow swf,
			final String domain,
			final String taskList,
			final String identity,
			final ExecutorService service,
			final TaskExecutor executor,
			final Duration timeout) {
		super();
		this.client = swf;
		TaskPoller poller = new TaskPoller(swf, domain, taskList, identity);
		this.provider = new ContextProviderThreadedImpl<TaskContext>(poller, service, timeout);
		this.executor = executor;
	}

	public void consume() {
		consume(true);
	}

	private void consume(final boolean failIfNoContext) {
		LOGGER.debug("Consuming next task context");
		TaskContext context = provider.get();
		if (context != null) {
			try {
				LOGGER.debug("Received a new task context to process");
				TaskReport report = new TaskReportImpl(client, context.contextId());
				executor.execute(context, report);
			} catch (Exception e) {
				throw new IllegalStateException("error during workflow decisions", e);
			}
		} else if (failIfNoContext) {
			LOGGER.debug("Failing because no task context available");
			throw new IllegalStateException("No context to consume !");
		} else {
			LOGGER.debug("no task context available");
		}
	}
}
