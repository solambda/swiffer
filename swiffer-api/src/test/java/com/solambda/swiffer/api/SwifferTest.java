package com.solambda.swiffer.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.Run;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.solambda.swiffer.test.Tests;

public class SwifferTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SwifferTest.class);

	private Worker worker;
	private Decider decider;

	@Retention(RetentionPolicy.RUNTIME)
	@WorkflowType(name = "test", version = "1")
	public static @interface TestWorkflow {

	}

	@ActivityType(name = "TestActivity", version = "1")
	public interface TestActivity {
	}

	public class TestActivityExecutor {
		@Executor(activity = TestActivity.class)
		public void executeTestActivity(String input) {
			LOGGER.info("Activity executed with input {} ", input);
		}
	}

	@TestWorkflow
	public class TestWorkflowTemplate {
	}

	@Test
	public void startShouldPassCorrectArguments() throws Exception {
		final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
		when(swf.startWorkflowExecution(any(StartWorkflowExecutionRequest.class)))
				.thenReturn(new Run().withRunId(""));
		final Swiffer swiffer = new Swiffer(swf, Tests.DOMAIN);
		//
		swiffer.startWorkflow(TestWorkflow.class, "wf-1", "testInput",
				new WorkflowOptions().childTerminationPolicy(ChildPolicy.REQUEST_CANCEL)
						.maxDecisionTaskDuration(WorkflowOptions.UNLIMITED)
						.maxWorkflowDuration(Duration.ofDays(10))
						.taskList("task-list-1")
						.taskPriority(123),
				Tags.of("t1", "t2"));

		//

		// verify
		final ArgumentCaptor<StartWorkflowExecutionRequest> captor = ArgumentCaptor
				.forClass(StartWorkflowExecutionRequest.class);
		verify(swf).startWorkflowExecution(captor.capture());

		final StartWorkflowExecutionRequest request = captor.getValue();
		//
		assertThat(request.getChildPolicy()).isEqualTo(ChildPolicy.REQUEST_CANCEL.name());
		assertThat(request.getDomain()).isEqualTo(Tests.DOMAIN);
		assertThat(request.getExecutionStartToCloseTimeout()).isEqualTo(Integer.toString(10 * 24 * 3600));
		assertThat(request.getInput()).isEqualTo("\"testInput\"");
		assertThat(request.getLambdaRole()).isEqualTo(null);
		assertThat(request.getTagList()).isEqualTo(Arrays.asList("t1", "t2"));
		assertThat(request.getTaskList().getName()).isEqualTo("task-list-1");
		assertThat(request.getTaskPriority()).isEqualTo("123");
		assertThat(request.getTaskStartToCloseTimeout()).isEqualTo("NONE");
		assertThat(request.getWorkflowId()).isEqualTo("wf-1");
		assertThat(request.getWorkflowType()).isEqualTo(
				new com.amazonaws.services.simpleworkflow.model.WorkflowType().withName("test").withVersion("1"));

	}

	/**
	 * Set to ignore because it takes too long to execute.
	 */
	@Ignore
	@Test
	public void getWorkflowExecutionHistory_paginated() {
		Random random = new Random();
		Swiffer swiffer = new Swiffer(Tests.swf(), Tests.DOMAIN);
		startWorker(swiffer);
		startDecider(swiffer);

		String workflowId = "WF-TEST-" + random.nextInt();
		String runId = swiffer.startWorkflow(TestWorkflow.class, workflowId, "some input",
											 new WorkflowOptions().maxWorkflowDuration(Duration.ofMinutes(1)),
											 null);
		swiffer.sendSignalToWorkflow(workflowId, "test-signal", "signalInput");

		List<HistoryEvent> workflowExecutionHistory = swiffer.getWorkflowExecutionHistory(workflowId, runId, 5, 1, true);

		assertThat(workflowExecutionHistory).isNotNull().extracting("eventType").contains("WorkflowExecutionSignaled");
	}

	@Ignore
	@Test
	public void getWorkflowExecutionHistory() {
		Random random = new Random();
		Swiffer swiffer = new Swiffer(Tests.swf(), Tests.DOMAIN);
		startWorker(swiffer);
		startDecider(swiffer);

		String workflowId = "WF-TEST-2" + random.nextInt();
		String runId = swiffer.startWorkflow(TestWorkflow.class, workflowId, "some input",
											 new WorkflowOptions().maxWorkflowDuration(Duration.ofMinutes(1)),
											 null);
		swiffer.sendSignalToWorkflow(workflowId, "test-signal", "signalInput");

		List<HistoryEvent> workflowExecutionHistory = swiffer.getWorkflowExecutionHistory(workflowId, runId);

		assertThat(workflowExecutionHistory).hasSize(5);
		assertThat(workflowExecutionHistory.get(4)).extracting("eventType").contains("WorkflowExecutionStarted");
	}

	@After
	public void tearDown() throws Exception {
		if (worker != null){
			worker.stop();
		}

		if (decider != null){
			decider.stop();
		}
	}

	private void startWorker(Swiffer swiffer ){
		worker = swiffer.newWorkerBuilder()
								 .taskList("default")
								 .identity("test-worker")
								 .executors(new TestActivityExecutor())
								 .build();
		worker.start();
	}

	private void startDecider(Swiffer swiffer) {
		decider = swiffer.newDeciderBuilder()
							  .identity("test-decider")
							  .workflowTemplates(new TestWorkflowTemplate())
							  .build();
		decider.start();
	}
}
