package com.solambda.swiffer.api.internal.decisions;

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
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.google.common.base.Stopwatch;
import com.solambda.swiffer.api.exceptions.TaskContextPollingException;

public class DecisionTaskContextPollerTest {

	private static final String TOKEN = "token";

	@Test
	public void polling_shouldReturnADecisionTaskContext() throws Exception {
		// GIVEN
		final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
		final DecisionTask task = new DecisionTask().withTaskToken(TOKEN);
		when(swf.pollForDecisionTask(any())).thenReturn(task);
		final DecisionTaskPoller poller = new DecisionTaskPoller(swf, "domain", "DecisionTaskList", "decisionpoller");
		// WHEN start polling
		final DecisionTaskContext taskContext = poller.poll();
		// THEN
		assertThat(taskContext.taskToken()).isEqualTo(TOKEN);
	}

	@Test
	public void pollingFailure_throwsAnException() throws Exception {
		// GIVEN
		final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
		final String expectedMessage = "exception for testing";
		when(swf.pollForDecisionTask(any())).then(failWith(new Exception(expectedMessage)));
		final DecisionTaskPoller poller = new DecisionTaskPoller(swf, "domain", "DecisionTaskList", "activit");
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
		final DecisionTask task = new DecisionTask().withTaskToken(TOKEN);
		final int expectedPollingDuration = 500;
		when(swf.pollForDecisionTask(any()))
				.then(returnAfterDelay(task, Duration.ofMillis(expectedPollingDuration)));
		final DecisionTaskPoller poller = new DecisionTaskPoller(swf, "domain", "DecisionTaskList", "decisionpoller");
		// WHEN start polling
		final Stopwatch watch = Stopwatch.createStarted();
		final Future<DecisionTaskContext> pollingFuture = Executors.newSingleThreadExecutor()
				.submit(() -> poller.poll());
		final DecisionTaskContext context = pollingFuture.get(expectedPollingDuration + 100, MILLISECONDS);
		// THEN the polling operation blocked
		assertThat(watch.elapsed(MILLISECONDS)).isCloseTo(expectedPollingDuration, Offset.offset(20L));
		assertThat(context.taskToken()).isEqualTo(TOKEN);
	}

	// polling can be immediately interrupted
	@Test
	public void polling_cannotBeStoppedImmediately() {
		// GIVEN a long running polling operation
		final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
		final DecisionTask task = new DecisionTask().withTaskToken(TOKEN);
		final int expectedPollingDuration = 5000;
		when(swf.pollForDecisionTask(any()))
				.then(returnAfterDelay(task, Duration.ofMillis(expectedPollingDuration)));
		final DecisionTaskPoller poller = new DecisionTaskPoller(swf, "domain", "DecisionTaskList", "decisionpoller");
		final Future<DecisionTaskContext> pollingFuture = Executors.newSingleThreadExecutor()
				.submit(() -> poller.poll());
		// WHEN stop polling
		sleep(ofMillis(300));
		poller.stop();
		// THEN the operation stops
		try {
			final DecisionTaskContext context = pollingFuture.get(400, TimeUnit.MILLISECONDS);
			fail("should have failed but got context " + context);
		} catch (final CancellationException e) {
			fail("unepected exception", e);
		} catch (final ExecutionException e) {
			assertThat(e.getCause()).isExactlyInstanceOf(CancellationException.class);
		} catch (final InterruptedException e) {
			fail("unepected exception", e);
		} catch (final TimeoutException e) {
			assertThat(e).isNotNull();
		}

	}

}
