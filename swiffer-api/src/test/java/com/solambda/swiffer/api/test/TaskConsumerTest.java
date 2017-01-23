package com.solambda.swiffer.api.test;

import static org.assertj.core.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.swiffer.api.internal.activities.ActivityExecutionReporter;
import com.solambda.swiffer.api.internal.activities.ActivityExecutionReporterImpl;
import com.solambda.swiffer.api.internal.activities.ActivityExecutor;
import com.solambda.swiffer.api.internal.activities.ActivityTaskContext;
import com.solambda.swiffer.api.internal.activities.ActivityTaskContextProvider;
import com.solambda.swiffer.api.internal.activities.ActivityTaskPoller;

public class TaskConsumerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityTaskPoller.class);
	private final ExecutorService executorService = Executors.newFixedThreadPool(10);
	private ActivityTaskContextProvider contextProvider;
	private ActivityExecutor executor;

	public TaskConsumerTest(final ActivityTaskContextProvider contextProvider, final ActivityExecutor executor) {
		super();
		this.contextProvider = contextProvider;
		this.executor = executor;
	}

	public void consume() {
		final ActivityTaskContext context = getContextTimeout(Duration.ofSeconds(5));
		if (context != null) {
			try {
				final ActivityExecutionReporter report = new ActivityExecutionReporterImpl(ObjectMother.client(),
						context.taskToken());
				LOGGER.debug("executing task context {}", context);
				// this.executor.execute(context, report);
			} catch (final Exception e) {
				fail("error during task activity execution", e);
			}
		} else {
			fail("impossible to consume activity task: there is not !");
		}
	}

	private ActivityTaskContext getContextTimeout(final Duration ofSeconds) {
		final Future<ActivityTaskContext> context = this.executorService.submit(() -> this.contextProvider.get());
		try {
			return context.get(ofSeconds.getSeconds(), TimeUnit.SECONDS);
		} catch (final TimeoutException e) {
			return null;
		} catch (final Exception e) {
			throw new IllegalStateException("cannot poll for acivity task", e);
		}
	}
}
