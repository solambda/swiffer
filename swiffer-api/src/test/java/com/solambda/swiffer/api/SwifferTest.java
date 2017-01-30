package com.solambda.swiffer.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.Run;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.solambda.swiffer.test.Tests;

public class SwifferTest {

	@WorkflowType(name = "test", version = "1")
	public static interface TestWorkflow {

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
		assertThat(request.getInput()).isEqualTo("testInput");
		assertThat(request.getLambdaRole()).isEqualTo(null);
		assertThat(request.getTagList()).isEqualTo(Arrays.asList("t1", "t2"));
		assertThat(request.getTaskList().getName()).isEqualTo("task-list-1");
		assertThat(request.getTaskPriority()).isEqualTo("123");
		assertThat(request.getTaskStartToCloseTimeout()).isEqualTo("NONE");
		assertThat(request.getWorkflowId()).isEqualTo("wf-1");
		assertThat(request.getWorkflowType()).isEqualTo(
				new com.amazonaws.services.simpleworkflow.model.WorkflowType().withName("test").withVersion("1"));

	}
}
