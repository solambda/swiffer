package com.solambda.swiffer.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.solambda.swiffer.api.model.Workflow;
import com.solambda.swiffer.api.model.WorkflowBuilder;
import com.solambda.swiffer.api.model.decider.ContextProvider;
import com.solambda.swiffer.api.model.decider.EventDelegatorDecider;
import com.solambda.swiffer.api.model.decider.impl.DecisionContextProviderImpl;
import com.solambda.swiffer.api.model.tasks.*;
import com.solambda.swiffer.api.model.tasks.impl.TaskPoller;
import com.solambda.swiffer.api.registries.TaskRegistry;
import com.solambda.swiffer.api.test.ObjectMother;
import com.solambda.swiffer.api.test.TaskConsumerTest;
import com.solambda.swiffer.api.test.WorkflowConsumerTest;

public class TestTaskExecutor {
	private EventDelegatorDecider delegator = new EventDelegatorDecider(ObjectMother.mockedRegistry());

	private ContextProvider provider = new DecisionContextProviderImpl(ObjectMother.client(), ObjectMother.domainName(), null,
			TestEventDelegatorDeciderIsNotified.class.getName());
	private WorkflowConsumerTest decisionConsumer = new WorkflowConsumerTest(delegator, provider);

	private WorkflowBuilder builder =
			new WorkflowBuilder()
					.client(ObjectMother.client())
					.domain(ObjectMother.domainName())
					.type(ObjectMother.registeredWorkflowType())
					.id(this.getClass().getName());
	private Workflow workflow = builder.build();
	private TaskContextProvider taskContextProvider = new TaskPoller(ObjectMother.client(), ObjectMother.domainName(),
			"default",
			TestTaskExecutor.class.getName());
	private TaskExecutor taskExecutor = mock(TaskExecutor.class);
	private TaskConsumerTest taskConsumer = new TaskConsumerTest(taskContextProvider, taskExecutor);

	private TaskRegistry registry = new TaskRegistry(ObjectMother.client());

	@Before
	public void registerTaskIfNeeded() {
		ObjectMother.resetMocks();
		try {
			registry.create(ObjectMother.domainName(), ObjectMother.taskType(), ObjectMother.taskDescription());
		} catch (Exception e) {
		}
	}

	@Test
	public void whenATaskIsScheduledItIsExecuted() throws Exception {
		String inputSent = "my-input";
		ObjectMother.whenWorkflowStarted((c, then) -> {
			then.scheduleTask(ObjectMother.taskType(), inputSent, TaskOptions.inTaskList("default"));
		});
		workflow.start();
		decisionConsumer.consume();
		taskConsumer.consume();
		ArgumentCaptor<TaskContext> captor = ArgumentCaptor.forClass(TaskContext.class);
		verify(taskExecutor).execute(captor.capture(), any());
		assertThat(captor.getValue().input()).isEqualTo(inputSent);
		assertThat(captor.getValue().taskType()).isEqualTo(ObjectMother.taskType());
		assertThat(captor.getValue().taskId()).isNotNull();
		// start wf
		// schedule a task
		// execute the task
		//

	}

}
