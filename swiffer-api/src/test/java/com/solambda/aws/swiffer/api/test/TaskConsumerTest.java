package com.solambda.aws.swiffer.api.test;

import static org.assertj.core.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.aws.swiffer.api.model.tasks.*;
import com.solambda.aws.swiffer.api.model.tasks.impl.TaskPoller;
import com.solambda.aws.swiffer.api.model.tasks.impl.TaskReportImpl;

public class TaskConsumerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskPoller.class);
	private final ExecutorService executorService = Executors.newFixedThreadPool(10);
	private TaskContextProvider contextProvider;
	private TaskExecutor executor;

	public TaskConsumerTest(final TaskContextProvider contextProvider, final TaskExecutor executor) {
		super();
		this.contextProvider = contextProvider;
		this.executor = executor;
	}

	public void consume() {
		TaskContext context = getContextTimeout(Duration.ofSeconds(5));
		if (context != null) {
			try {
				TaskReport report = new TaskReportImpl(ObjectMother.client(), context.contextId());
				LOGGER.debug("executing task context {}", context);
				executor.execute(context, report);
			} catch (Exception e) {
				fail("error during task activity execution", e);
			}
		} else {
			fail("impossible to consume activity task: there is not !");
		}
	}

	private TaskContext getContextTimeout(final Duration ofSeconds) {
		Future<TaskContext> context = executorService.submit(() -> contextProvider.get());
		try {
			return context.get(ofSeconds.getSeconds(), TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			return null;
		} catch (Exception e) {
			throw new IllegalStateException("cannot poll for acivity task", e);
		}
	}
}
