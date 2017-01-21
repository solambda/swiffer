package com.solambda.aws.swiffer.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.*;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.solambda.aws.swiffer.api.model.*;
import com.solambda.aws.swiffer.api.model.decider.*;
import com.solambda.aws.swiffer.api.model.decider.context.*;
import com.solambda.aws.swiffer.api.model.decider.handler.*;
import com.solambda.aws.swiffer.api.model.decider.impl.DecisionContextProviderImpl;
import com.solambda.aws.swiffer.api.model.tasks.TaskContextProvider;
import com.solambda.aws.swiffer.api.model.tasks.TaskExecutor;
import com.solambda.aws.swiffer.api.model.tasks.TaskReport;
import com.solambda.aws.swiffer.api.model.tasks.impl.TaskPoller;
import com.solambda.aws.swiffer.api.test.ObjectMother;
import com.solambda.aws.swiffer.api.test.TaskConsumerTest;
import com.solambda.aws.swiffer.api.test.WorkflowConsumerTest;

public class TestEventDelegatorDeciderIsNotified {

	private EventContextHandlerRegistry registry;
	private EventDelegatorDecider delegator;

	private Workflow workflow;

	private WorkflowConsumerTest consumer;

	private WorkflowBuilder builder;

	private WorkflowStartedHandler workflowfStartedHandler = mock(WorkflowStartedHandler.class);
	private WorkflowTerminatedHandler workflowfTerminatedHandler = mock(WorkflowTerminatedHandler.class);
	private WorkflowCancelRequestedHandler workflowfCancelRequestedHandler = mock(WorkflowCancelRequestedHandler.class);
	private SignalReceivedHandler signalReceivedHandler = mock(SignalReceivedHandler.class);
	private TaskCompletedHandler taskCompletedHandler = mock(TaskCompletedHandler.class);
	private TaskTimedOutHandler taskTimedOutHandler = mock(TaskTimedOutHandler.class);
	private TaskFailedHandler taskFailedHandler = mock(TaskFailedHandler.class);
	private TaskScheduleFailedHandler taskScheduleFailedHandler = mock(TaskScheduleFailedHandler.class);
	private TimerFiredHandler timerFiredHandler = mock(TimerFiredHandler.class);
	private TimerCanceledHandler timerCanceledHandler = mock(TimerCanceledHandler.class);
	private MarkerRecordedHandler markerRecorderHandler = mock(MarkerRecordedHandler.class);

	public TestEventDelegatorDeciderIsNotified() {
		EventContextHandlerRegistryBuilder registryBuilder = new EventContextHandlerRegistryBuilder(ObjectMother.registeredWorkflowType());
		registryBuilder
				.onWorkflow()
				.started(workflowfStartedHandler)
				.terminated(workflowfTerminatedHandler)
				.cancelRequested(workflowfCancelRequestedHandler);
		registryBuilder
				.on(ObjectMother.taskType())
				.completed(taskCompletedHandler)
				.failed(taskFailedHandler)
				.timedOut(taskTimedOutHandler);
		registryBuilder
				.on(ObjectMother.unregisteredTaskType())
				.scheduleFailed(taskScheduleFailedHandler);

		registryBuilder
				.on(ObjectMother.smallTimeoutTaskType())
				.timedOut(taskTimedOutHandler);

		registryBuilder.on(ObjectMother.signalName()).received(signalReceivedHandler);
		registryBuilder.on(ObjectMother.timerName()).fired(timerFiredHandler);
		registryBuilder.on(ObjectMother.timerName()).canceled(timerCanceledHandler);

		registryBuilder.on(ObjectMother.markerName()).recorded(markerRecorderHandler);
		registry = registryBuilder.build();
		delegator = new EventDelegatorDecider(registry);
		ContextProvider<DecisionContext> provider = new DecisionContextProviderImpl(ObjectMother.client(), ObjectMother.domainName(), null,
				TestEventDelegatorDeciderIsNotified.class.getName());
		consumer = new WorkflowConsumerTest(delegator, provider);
		builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.registeredWorkflowType())
				.id(this.getClass().getName());
	}

	@Before
	public void startMock() {
		reset(workflowfStartedHandler, workflowfCancelRequestedHandler, workflowfTerminatedHandler,
				taskCompletedHandler, taskFailedHandler, taskScheduleFailedHandler, taskTimedOutHandler,
				timerFiredHandler, timerCanceledHandler,
				signalReceivedHandler,
				markerRecorderHandler);
		ObjectMother.resetMocks();
		workflow = builder.build();
	}

	@After
	public void terminate() {
		workflow.terminate();
	}

	@Test
	public void whenTheWorkflowIsStartedFromOutside() {
		String inputSent = "my-input";
		// WHEN
		workflow.start(inputSent);
		consumer.consume();
		// THEN
		ArgumentCaptor<WorkflowStartedContext> captor = ArgumentCaptor.forClass(WorkflowStartedContext.class);
		verify(workflowfStartedHandler).onWorkflowStarted(captor.capture(), any());
		assertThat(captor.getValue().input()).isEqualTo(inputSent);
	}

	@Test
	public void whenTheWorkflowIsSignaledFromOutside() {
		String signalInputSent = "my-signal-input";
		// WHEN
		workflow.start();
		workflow.signal(ObjectMother.signalName().name(), signalInputSent);
		consumer.consume();
		// THEN
		ArgumentCaptor<SignalReceivedContext> captor = ArgumentCaptor.forClass(SignalReceivedContext.class);
		verify(signalReceivedHandler).onSignalReceived(captor.capture(), any());
		assertThat(captor.getValue().input()).isEqualTo(signalInputSent);
		assertThat(captor.getValue().signalName()).isEqualTo(ObjectMother.signalName().name());

	}

	@Test
	public void whenTheWorkflowIsRequestedToCancelFromOutside() {
		// WHEN
		workflow.start();
		workflow.requestCancel();
		consumer.consume();

		// THEN
		ArgumentCaptor<WorkflowCancelRequestedContext> captor = ArgumentCaptor.forClass(WorkflowCancelRequestedContext.class);
		verify(workflowfCancelRequestedHandler).onWorkflowCancelRequested(captor.capture(), any());
		assertThat(captor.getValue().cause()).isNull();
	}

	@Test
	public void whenATaskIsCompleted() throws Exception {
		// WHEN
		String output = "my-output";
		scheduleTask();
		completeTask(output);
		consumer.consume();
		// THEN
		ArgumentCaptor<TaskCompletedContext> captor = ArgumentCaptor.forClass(TaskCompletedContext.class);
		verify(taskCompletedHandler).onTaskCompleted(captor.capture(), any());
		assertThat(captor.getValue().output()).isEqualTo(output);
		assertThat(captor.getValue().taskType()).isEqualTo(ObjectMother.taskType());
	}

	@Test
	public void whenATaskIsFailed() throws Exception {
		// WHEN
		String details = "my-details";
		String reason = "my-reason";
		scheduleTask();
		failTask(reason, details);
		consumer.consume();
		// THEN
		ArgumentCaptor<TaskFailedContext> captor = ArgumentCaptor.forClass(TaskFailedContext.class);
		verify(taskFailedHandler).onTaskFailed(captor.capture(), any());
		assertThat(captor.getValue().reason()).isEqualTo(reason);
		assertThat(captor.getValue().details()).isEqualTo(details);
		assertThat(captor.getValue().taskType()).isEqualTo(ObjectMother.taskType());
	}

	@Test
	@Ignore("timeout")
	public void whenATaskIsTimedOutAtExecution() throws Exception {
		// WHEN
		scheduleTask(ObjectMother.smallTimeoutTaskType());
		timeoutTaskExecution();
		consumer.consume();
		// THEN
		ArgumentCaptor<TaskTimedOutContext> captor = ArgumentCaptor.forClass(TaskTimedOutContext.class);
		verify(taskTimedOutHandler).onTaskTimedOut(captor.capture(), any());
		assertThat(captor.getValue().reason()).isEqualTo("START_TO_CLOSE");
		assertThat(captor.getValue().taskType()).isEqualTo(ObjectMother.smallTimeoutTaskType());
	}

	@Test
	@Ignore("does not work!")
	public void whenATaskIsTimedOutBeforeExecution() throws Exception {
		// WHEN
		scheduleTask(ObjectMother.smallTimeoutTaskType());
		timeoutTaskBeforeExecution();
		consumer.consume();
		// THEN
		ArgumentCaptor<TaskTimedOutContext> captor = ArgumentCaptor.forClass(TaskTimedOutContext.class);
		verify(taskTimedOutHandler).onTaskTimedOut(captor.capture(), any());
		assertThat(captor.getValue().reason()).isEqualTo("SCHEDULE_TO_START");
		assertThat(captor.getValue().taskType()).isEqualTo(ObjectMother.smallTimeoutTaskType());
	}

	@Test
	@Ignore("P2")
	public void whenATaskIsCanceled() throws Exception {
		throw new IllegalStateException("not yet impleemnts");
	}

	@Test
	public void whenSchedulingATaskFailed() throws Exception {
		// WHEN
		workflow.start();
		ObjectMother.makeDecider(to -> to.scheduleTask(ObjectMother.unregisteredTaskType()))
				.when(workflowfStartedHandler).onWorkflowStarted(any(), any());
		consumer.consume();
		consumer.consume();

		// THEN
		ArgumentCaptor<TaskScheduleFailedContext> captor = ArgumentCaptor.forClass(TaskScheduleFailedContext.class);
		verify(taskScheduleFailedHandler).onTaskScheduleFailed(captor.capture(), any());
		assertThat(captor.getValue().cause()).isEqualTo("ACTIVITY_TYPE_DOES_NOT_EXIST");
		assertThat(captor.getValue().taskId()).isNotNull();
		assertThat(captor.getValue().taskType()).isEqualTo(ObjectMother.unregisteredTaskType());

	}

	@Test
	@Ignore("priority 2")
	public void whenATaskIsStarted() throws Exception {
		throw new IllegalStateException("not yet impleemnts");
	}

	@Test
	@Ignore("priority 2")
	public void whenATaskIsScheduled() throws Exception {
		throw new IllegalStateException("not yet impleemnts");
	}

	@Test
	@Ignore("priority 2")
	public void whenATaskIsRequestedToCancel() throws Exception {
		throw new IllegalStateException("not yet impleemnts");
	}

	@Test
	@Ignore("priority 2")
	public void whenRequestingATaskToCancelFailed() throws Exception {
		throw new IllegalStateException("not yet impleemnts");
	}

	@Test
	@Ignore("priority 2")
	public void whenTheWorkflowIsCompleted() throws Exception {
		// WHEN
		workflow.start();
		ObjectMother.makeDecider(to -> to.completeWorfklow()).when(workflowfStartedHandler).onWorkflowStarted(any(), any());
		consumer.consume();

		throw new IllegalStateException("not yet impleemnts");
	}

	@Test
	@Ignore("priority 2")
	public void whenTheWorkflowIsFailed() throws Exception {
		throw new IllegalStateException("not yet impleemnts");
	}

	@Test
	@Ignore("priority 2")
	public void whenTheWorkflowIsCanceled() throws Exception {
		throw new IllegalStateException("not yet impleemnts");
	}

	@Test
	@Ignore("once terminated, we can't get a chance to get a decision task")
	public void whenTheWorkflowIsTerminatedFromOutside() {
		// WHEN
		String reasonSent = "my-reason";
		String detailsSent = "my-details";
		workflow.start();
		workflow.terminate(Failure.reason(reasonSent).details(detailsSent), ChildPolicy.REQUEST_CANCEL);
		consumer.consume();
		// THEN
		ArgumentCaptor<WorkflowTerminatedContext> captor = ArgumentCaptor.forClass(WorkflowTerminatedContext.class);
		verify(workflowfTerminatedHandler).onWorkflowTerminated(captor.capture());
		assertThat(captor.getValue().reason()).isEqualTo(reasonSent);
		assertThat(captor.getValue().details()).isEqualTo(detailsSent);
		assertThat(captor.getValue().cause()).isNull();
	}

	@Test
	public void whenATimerFired() throws Exception {
		String timerName = "my-timer";
		String input = "my-timer-input";
		// WHEN
		startTimer(timerName, input, 5);
		ObjectMother.sleep(Duration.ofSeconds(4));
		consumer.consume();
		// THEN
		ArgumentCaptor<TimerFiredContext> captor = ArgumentCaptor.forClass(TimerFiredContext.class);
		verify(timerFiredHandler).onTimerFired(captor.capture(), any());
		assertThat(captor.getValue().timerId()).isEqualTo(timerName);
		assertThat(captor.getValue().control()).isEqualTo(input);
	}

	@Test
	public void whenATimerCanceledWithForceAttribute() throws Exception {
		String input = "my-timer-input";
		// WHEN starting workflow
		ObjectMother.makeDecider(to -> {
			to.startTimer(ObjectMother.timerName().name(), input, Duration.ofSeconds(10));
			to.scheduleTask(ObjectMother.taskType());
		}).when(workflowfStartedHandler).onWorkflowStarted(any(), any());
		//
		workflow.start();
		consumer.consume();// start timer and schedule task
		ObjectMother.makeDecider(to -> {
			to.cancelTimer(ObjectMother.timerName().name(), true);
		}).when(taskCompletedHandler).onTaskCompleted(any(), any());
		completeTask("ok");// complete task
		ObjectMother.sleep(Duration.ofSeconds(2));
		consumer.consume();// cancel timer
		consumer.consume();// notify
		// THEN
		ArgumentCaptor<TimerCanceledContext> captor = ArgumentCaptor.forClass(TimerCanceledContext.class);
		verify(timerCanceledHandler).onTimerCanceled(captor.capture(), any());
		assertThat(captor.getValue().timerId()).isEqualTo(ObjectMother.timerName().name());
		assertThat(captor.getValue().control()).isEqualTo(input);
	}

	@Test
	public void whenAMarkerIsRecorded() {
		String markerName = "my-marker";
		String markerDetails = "my-marker-details";
		// start workflow
		ObjectMother.makeDecider(to -> {
			to.scheduleTask(ObjectMother.taskType());
			to.createMarker(markerName, markerDetails);
		}).when(workflowfStartedHandler).onWorkflowStarted(any(), any());
		workflow.start();
		consumer.consume();// start timer and schedule task
		completeTask("");
		// consume => the marker recorded is called
		consumer.consume();
		// THEN
		ArgumentCaptor<MarkerRecordedContext> captor = ArgumentCaptor.forClass(MarkerRecordedContext.class);
		verify(markerRecorderHandler).onMarkerRecorded(captor.capture(), any());
		assertThat(captor.getValue().markerName()).isEqualTo(markerName);
		assertThat(captor.getValue().details()).isEqualTo(markerDetails);
	}

	private void completeTask(final String output) {
		TaskContextProvider taskContextProvider = new TaskPoller(ObjectMother.client(), ObjectMother.domainName(),
				"default",
				this.getClass().getName());
		TaskExecutor taskExecutor = mock(TaskExecutor.class);
		doAnswer(i -> {
			i.getArgumentAt(1, TaskReport.class).completed(output);
			return null;
		}).when(taskExecutor).execute(any(), any());
		TaskConsumerTest taskConsumer = new TaskConsumerTest(taskContextProvider, taskExecutor);
		taskConsumer.consume();
	}

	private void failTask(final String reason, final String details) {
		TaskContextProvider taskContextProvider = new TaskPoller(ObjectMother.client(), ObjectMother.domainName(),
				"default",
				this.getClass().getName());
		TaskExecutor taskExecutor = mock(TaskExecutor.class);
		doAnswer(i -> {
			i.getArgumentAt(1, TaskReport.class).failed(Failure.reason(reason).details(details));
			return null;
		}).when(taskExecutor).execute(any(), any());
		TaskConsumerTest taskConsumer = new TaskConsumerTest(taskContextProvider, taskExecutor);
		taskConsumer.consume();
	}

	private void timeoutTaskBeforeExecution() {
		ObjectMother.sleep(Duration.ofSeconds(5));
	}

	private void timeoutTaskExecution() {
		TaskContextProvider taskContextProvider = new TaskPoller(ObjectMother.client(), ObjectMother.domainName(),
				"default",
				this.getClass().getName());
		TaskExecutor taskExecutor = mock(TaskExecutor.class);
		doAnswer(i -> {
			ObjectMother.sleep(Duration.ofSeconds(5));
			return null;
		}).when(taskExecutor).execute(any(), any());
		TaskConsumerTest taskConsumer = new TaskConsumerTest(taskContextProvider, taskExecutor);
		taskConsumer.consume();
	}

	private void scheduleTask() {
		scheduleTask(ObjectMother.taskType());
	}

	private void scheduleTask(final TaskType taskType) {
		ObjectMother.makeDecider(to -> to.scheduleTask(taskType))
				.when(workflowfStartedHandler).onWorkflowStarted(any(), any());
		workflow.start();
		consumer.consume();
	}

	private void startTimer(final String timerName, final String input, final int timeoutInSeconds) {
		ObjectMother.makeDecider(to -> to.startTimer(timerName, input, Duration.ofSeconds(timeoutInSeconds)))
				.when(workflowfStartedHandler).onWorkflowStarted(any(), any());
		workflow.start();
		consumer.consume();
	}

}
