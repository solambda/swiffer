package com.solambda.swiffer.api.internal.activities;

import static com.solambda.swiffer.test.Tests.failWith;
import static com.solambda.swiffer.test.Tests.returnAfterDelay;
import static com.solambda.swiffer.test.Tests.sleep;
import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.assertj.core.data.Offset;
import org.junit.Test;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.google.common.base.Stopwatch;
import com.solambda.swiffer.api.exceptions.TaskContextPollingException;

public class ActivityTaskContextPollerTest {

	private static final String TOKEN = "token";

	@Test
	public void polling_shouldReturnAnActivityTaskContext() throws Exception {
		// GIVEN
		final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
		final ActivityTask task = new ActivityTask().withTaskToken(TOKEN);
		when(swf.pollForActivityTask(any())).thenReturn(task);
		final ActivityTaskPoller poller = new ActivityTaskPoller(swf, "domain", "activityTaskList", "activit");
		// WHEN start polling
		final ActivityTaskContext taskContext = poller.poll();
		// THEN
		assertThat(taskContext.taskToken()).isEqualTo(TOKEN);
	}

	@Test
	public void pollingFailure_throwsAnException() throws Exception {
		// GIVEN
		final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
		final String expectedMessage = "exception for testing";
		when(swf.pollForActivityTask(any())).then(failWith(new Exception(expectedMessage)));
		final ActivityTaskPoller poller = new ActivityTaskPoller(swf, "domain", "activityTaskList", "activit");
		// WHEN start polling THEN correct exception is thrown
		assertThatExceptionOfType(TaskContextPollingException.class)
				.isThrownBy(() -> poller.poll())
				.withRootCauseExactlyInstanceOf(Exception.class)
				.withStackTraceContaining(expectedMessage);

	}

	@Test
	public void polling_isABlockingOperation() throws Exception {
		// GIVEN a blocking polling operation
		final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
		final ActivityTask task = new ActivityTask().withTaskToken(TOKEN);
		final int expectedPollingDuration = 500;
		when(swf.pollForActivityTask(any()))
				.then(returnAfterDelay(task, Duration.ofMillis(expectedPollingDuration)));
		final ActivityTaskPoller poller = new ActivityTaskPoller(swf, "domain", "activityTaskList", "activit");
		// WHEN start polling
		final Stopwatch watch = Stopwatch.createStarted();
		final Future<ActivityTaskContext> pollingFuture = Executors.newSingleThreadExecutor()
				.submit(() -> poller.poll());
		final ActivityTaskContext context = pollingFuture.get(expectedPollingDuration + 200, MILLISECONDS);
		// THEN the polling operation blocked
		assertThat(watch.elapsed(MILLISECONDS)).isCloseTo(expectedPollingDuration, Offset.offset(100L));
		assertThat(context.taskToken()).isEqualTo(TOKEN);
	}

	// polling can be immediately interrupted
	@Test
	public void polling_canBeStopped() {
		// GIVEN a long running polling operation
		final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
		final ActivityTask task = new ActivityTask().withTaskToken(TOKEN);
		final int expectedPollingDuration = 5000;
		when(swf.pollForActivityTask(any()))
				.then(returnAfterDelay(task, Duration.ofMillis(expectedPollingDuration)));
		final ActivityTaskPoller poller = new ActivityTaskPoller(swf, "domain", "activityTaskList", "activit");
		final Future<ActivityTaskContext> pollingFuture = Executors.newSingleThreadExecutor()
				.submit(() -> poller.poll());
		// WHEN stop polling
		sleep(ofMillis(300));
		poller.stop();
		// THEN the operation stops
		try {
			final ActivityTaskContext context = pollingFuture.get(100, TimeUnit.MILLISECONDS);
			fail("should have failed but got context " + context);
		} catch (final CancellationException e) {
			fail("unepected exception", e);
		} catch (final ExecutionException e) {
			assertThat(e.getCause()).isExactlyInstanceOf(CancellationException.class);
		} catch (final InterruptedException e) {
			fail("unepected exception", e);
		} catch (final TimeoutException e) {
			fail("should not have timedout", e);
		}

	}

}
