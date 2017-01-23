package com.solambda.swiffer.api.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.solambda.swiffer.api.internal.Failure;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.model.Workflow;
import com.solambda.swiffer.api.model.WorkflowBuilder;
import com.solambda.swiffer.api.model.decider.ContextProvider;
import com.solambda.swiffer.api.model.decider.DecisionContext;
import com.solambda.swiffer.api.model.decider.EventContextHandlerRegistry;
import com.solambda.swiffer.api.model.decider.EventContextHandlerRegistryBuilder;
import com.solambda.swiffer.api.model.decider.EventDelegatorDecider;
import com.solambda.swiffer.api.model.decider.context.MarkerRecordedContext;
import com.solambda.swiffer.api.model.decider.context.SignalReceivedContext;
import com.solambda.swiffer.api.model.decider.context.TaskCompletedContext;
import com.solambda.swiffer.api.model.decider.context.TaskFailedContext;
import com.solambda.swiffer.api.model.decider.context.TaskScheduleFailedContext;
import com.solambda.swiffer.api.model.decider.context.TaskTimedOutContext;
import com.solambda.swiffer.api.model.decider.context.TimerCanceledContext;
import com.solambda.swiffer.api.model.decider.context.TimerFiredContext;
import com.solambda.swiffer.api.model.decider.context.WorkflowCancelRequestedContext;
import com.solambda.swiffer.api.model.decider.context.WorkflowStartedContext;
import com.solambda.swiffer.api.model.decider.context.WorkflowTerminatedContext;
import com.solambda.swiffer.api.model.decider.handler.MarkerRecordedHandler;
import com.solambda.swiffer.api.model.decider.handler.SignalReceivedHandler;
import com.solambda.swiffer.api.model.decider.handler.TaskCompletedHandler;
import com.solambda.swiffer.api.model.decider.handler.TaskFailedHandler;
import com.solambda.swiffer.api.model.decider.handler.TaskScheduleFailedHandler;
import com.solambda.swiffer.api.model.decider.handler.TaskTimedOutHandler;
import com.solambda.swiffer.api.model.decider.handler.TimerCanceledHandler;
import com.solambda.swiffer.api.model.decider.handler.TimerFiredHandler;
import com.solambda.swiffer.api.model.decider.handler.WorkflowCancelRequestedHandler;
import com.solambda.swiffer.api.model.decider.handler.WorkflowStartedHandler;
import com.solambda.swiffer.api.model.decider.handler.WorkflowTerminatedHandler;
import com.solambda.swiffer.api.model.decider.impl.DecisionContextProviderImpl;

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
		final EventContextHandlerRegistryBuilder registryBuilder = new EventContextHandlerRegistryBuilder(
				ObjectMother.registeredWorkflowType());
		registryBuilder
				.onWorkflow()
				.started(this.workflowfStartedHandler)
				.terminated(this.workflowfTerminatedHandler)
				.cancelRequested(this.workflowfCancelRequestedHandler);
		registryBuilder
				.on(ObjectMother.taskType())
				.completed(this.taskCompletedHandler)
				.failed(this.taskFailedHandler)
				.timedOut(this.taskTimedOutHandler);
		registryBuilder
				.on(ObjectMother.unregisteredTaskType())
				.scheduleFailed(this.taskScheduleFailedHandler);

		registryBuilder
				.on(ObjectMother.smallTimeoutTaskType())
				.timedOut(this.taskTimedOutHandler);

		registryBuilder.on(ObjectMother.signalName()).received(this.signalReceivedHandler);
		registryBuilder.on(ObjectMother.timerName()).fired(this.timerFiredHandler);
		registryBuilder.on(ObjectMother.timerName()).canceled(this.timerCanceledHandler);

		registryBuilder.on(ObjectMother.markerName()).recorded(this.markerRecorderHandler);
		this.registry = registryBuilder.build();
		this.delegator = new EventDelegatorDecider(this.registry);
		final ContextProvider<DecisionContext> provider = new DecisionContextProviderImpl(ObjectMother.client(),
				ObjectMother.domainName(), null,
				TestEventDelegatorDeciderIsNotified.class.getName());
		this.consumer = new WorkflowConsumerTest(this.delegator, provider);
		this.builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.registeredWorkflowType())
				.id(this.getClass().getName());
	}

	@Before
	public void startMock() {
		reset(this.workflowfStartedHandler, this.workflowfCancelRequestedHandler, this.workflowfTerminatedHandler,
				this.taskCompletedHandler, this.taskFailedHandler, this.taskScheduleFailedHandler,
				this.taskTimedOutHandler,
				this.timerFiredHandler, this.timerCanceledHandler,
				this.signalReceivedHandler,
				this.markerRecorderHandler);
		ObjectMother.resetMocks();
		this.workflow = this.builder.build();
	}

	@After
	public void terminate() {
		this.workflow.terminate();
	}

	@Test
	public void whenTheWorkflowIsStartedFromOutside() {
		final String inputSent = "my-input";
		// WHEN
		this.workflow.start(inputSent);
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<WorkflowStartedContext> captor = ArgumentCaptor.forClass(WorkflowStartedContext.class);
		verify(this.workflowfStartedHandler).onWorkflowStarted(captor.capture(), any());
		assertThat(captor.getValue().input()).isEqualTo(inputSent);
	}

	@Test
	public void whenTheWorkflowIsSignaledFromOutside() {
		final String signalInputSent = "my-signal-input";
		// WHEN
		this.workflow.start();
		this.workflow.signal(ObjectMother.signalName().name(), signalInputSent);
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<SignalReceivedContext> captor = ArgumentCaptor.forClass(SignalReceivedContext.class);
		verify(this.signalReceivedHandler).onSignalReceived(captor.capture(), any());
		assertThat(captor.getValue().input()).isEqualTo(signalInputSent);
		assertThat(captor.getValue().signalName()).isEqualTo(ObjectMother.signalName().name());

	}

	@Test
	public void whenTheWorkflowIsRequestedToCancelFromOutside() {
		// WHEN
		this.workflow.start();
		this.workflow.requestCancel();
		this.consumer.consume();

		// THEN
		final ArgumentCaptor<WorkflowCancelRequestedContext> captor = ArgumentCaptor
				.forClass(WorkflowCancelRequestedContext.class);
		verify(this.workflowfCancelRequestedHandler).onWorkflowCancelRequested(captor.capture(), any());
		assertThat(captor.getValue().cause()).isNull();
	}

	@Test
	public void whenATaskIsCompleted() throws Exception {
		// WHEN
		final String output = "my-output";
		scheduleTask();
		completeTask(output);
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<TaskCompletedContext> captor = ArgumentCaptor.forClass(TaskCompletedContext.class);
		verify(this.taskCompletedHandler).onTaskCompleted(captor.capture(), any());
		assertThat(captor.getValue().output()).isEqualTo(output);
		assertThat(captor.getValue().taskType()).isEqualTo(ObjectMother.taskType());
	}

	@Test
	public void whenATaskIsFailed() throws Exception {
		// WHEN
		final String details = "my-details";
		final String reason = "my-reason";
		scheduleTask();
		failTask(reason, details);
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<TaskFailedContext> captor = ArgumentCaptor.forClass(TaskFailedContext.class);
		verify(this.taskFailedHandler).onTaskFailed(captor.capture(), any());
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
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<TaskTimedOutContext> captor = ArgumentCaptor.forClass(TaskTimedOutContext.class);
		verify(this.taskTimedOutHandler).onTaskTimedOut(captor.capture(), any());
		assertThat(captor.getValue().reason()).isEqualTo("START_TO_CLOSE");
		assertThat(captor.getValue().taskType()).isEqualTo(ObjectMother.smallTimeoutTaskType());
	}

	@Test
	@Ignore("does not work!")
	public void whenATaskIsTimedOutBeforeExecution() throws Exception {
		// WHEN
		scheduleTask(ObjectMother.smallTimeoutTaskType());
		timeoutTaskBeforeExecution();
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<TaskTimedOutContext> captor = ArgumentCaptor.forClass(TaskTimedOutContext.class);
		verify(this.taskTimedOutHandler).onTaskTimedOut(captor.capture(), any());
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
		this.workflow.start();
		ObjectMother.makeDecider(to -> to.scheduleTask(ObjectMother.unregisteredTaskType()))
				.when(this.workflowfStartedHandler).onWorkflowStarted(any(), any());
		this.consumer.consume();
		this.consumer.consume();

		// THEN
		final ArgumentCaptor<TaskScheduleFailedContext> captor = ArgumentCaptor
				.forClass(TaskScheduleFailedContext.class);
		verify(this.taskScheduleFailedHandler).onTaskScheduleFailed(captor.capture(), any());
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
		this.workflow.start();
		ObjectMother.makeDecider(to -> to.completeWorfklow()).when(this.workflowfStartedHandler).onWorkflowStarted(
				any(),
				any());
		this.consumer.consume();

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
		final String reasonSent = "my-reason";
		final String detailsSent = "my-details";
		this.workflow.start();
		this.workflow.terminate(Failure.reason(reasonSent).details(detailsSent), ChildPolicy.REQUEST_CANCEL);
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<WorkflowTerminatedContext> captor = ArgumentCaptor
				.forClass(WorkflowTerminatedContext.class);
		verify(this.workflowfTerminatedHandler).onWorkflowTerminated(captor.capture());
		assertThat(captor.getValue().reason()).isEqualTo(reasonSent);
		assertThat(captor.getValue().details()).isEqualTo(detailsSent);
		assertThat(captor.getValue().cause()).isNull();
	}

	@Test
	public void whenATimerFired() throws Exception {
		final String timerName = "my-timer";
		final String input = "my-timer-input";
		// WHEN
		startTimer(timerName, input, 5);
		ObjectMother.sleep(Duration.ofSeconds(4));
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<TimerFiredContext> captor = ArgumentCaptor.forClass(TimerFiredContext.class);
		verify(this.timerFiredHandler).onTimerFired(captor.capture(), any());
		assertThat(captor.getValue().timerId()).isEqualTo(timerName);
		assertThat(captor.getValue().control()).isEqualTo(input);
	}

	@Test
	public void whenATimerCanceledWithForceAttribute() throws Exception {
		final String input = "my-timer-input";
		// WHEN starting workflow
		ObjectMother.makeDecider(to -> {
			to.startTimer(ObjectMother.timerName().name(), input, Duration.ofSeconds(10));
			to.scheduleTask(ObjectMother.taskType());
		}).when(this.workflowfStartedHandler).onWorkflowStarted(any(), any());
		//
		this.workflow.start();
		this.consumer.consume();// start timer and schedule task
		ObjectMother.makeDecider(to -> {
			to.cancelTimer(ObjectMother.timerName().name(), true);
		}).when(this.taskCompletedHandler).onTaskCompleted(any(), any());
		completeTask("ok");// complete task
		ObjectMother.sleep(Duration.ofSeconds(2));
		this.consumer.consume();// cancel timer
		this.consumer.consume();// notify
		// THEN
		final ArgumentCaptor<TimerCanceledContext> captor = ArgumentCaptor.forClass(TimerCanceledContext.class);
		verify(this.timerCanceledHandler).onTimerCanceled(captor.capture(), any());
		assertThat(captor.getValue().timerId()).isEqualTo(ObjectMother.timerName().name());
		assertThat(captor.getValue().control()).isEqualTo(input);
	}

	@Test
	public void whenAMarkerIsRecorded() {
		final String markerName = "my-marker";
		final String markerDetails = "my-marker-details";
		// start workflow
		ObjectMother.makeDecider(to -> {
			to.scheduleTask(ObjectMother.taskType());
			to.createMarker(markerName, markerDetails);
		}).when(this.workflowfStartedHandler).onWorkflowStarted(any(), any());
		this.workflow.start();
		this.consumer.consume();// start timer and schedule task
		completeTask("");
		// consume => the marker recorded is called
		this.consumer.consume();
		// THEN
		final ArgumentCaptor<MarkerRecordedContext> captor = ArgumentCaptor.forClass(MarkerRecordedContext.class);
		verify(this.markerRecorderHandler).onMarkerRecorded(captor.capture(), any());
		assertThat(captor.getValue().markerName()).isEqualTo(markerName);
		assertThat(captor.getValue().details()).isEqualTo(markerDetails);
	}

	private void completeTask(final String output) {
		// final ActivityTaskContextProvider taskContextProvider = new
		// ActivityTaskPoller(ObjectMother.client(),
		// ObjectMother.domainName(),
		// "default",
		// this.getClass().getName());
		// final ActivityExecutor taskExecutor = mock(ActivityExecutor.class);
		// doAnswer(i -> {
		// String token;
		// i.<ActivityExecutionReporter>getArgument(1).completed(token,output);
		// return null;
		// }).when(taskExecutor).execute(any());
		// final TaskConsumerTest taskConsumer = new
		// TaskConsumerTest(taskContextProvider, taskExecutor);
		// taskConsumer.consume();
	}

	private void failTask(final String reason, final String details) {
		// final ActivityTaskContextProvider taskContextProvider = new
		// ActivityTaskPoller(ObjectMother.client(),
		// ObjectMother.domainName(),
		// "default",
		// this.getClass().getName());
		// final ActivityExecutor taskExecutor = mock(ActivityExecutor.class);
		// doAnswer(i -> {
		// i.<ActivityExecutionReporter>getArgument(1).failed(Failure.reason(reason).details(details));
		// return null;
		// }).when(taskExecutor).execute(any(), any());
		// final TaskConsumerTest taskConsumer = new
		// TaskConsumerTest(taskContextProvider, taskExecutor);
		// taskConsumer.consume();
	}

	private void timeoutTaskBeforeExecution() {
		ObjectMother.sleep(Duration.ofSeconds(5));
	}

	private void timeoutTaskExecution() {
		// final ActivityTaskContextProvider taskContextProvider = new
		// ActivityTaskPoller(ObjectMother.client(),
		// ObjectMother.domainName(),
		// "default",
		// this.getClass().getName());
		// final ActivityExecutor taskExecutor = mock(ActivityExecutor.class);
		// doAnswer(i -> {
		// ObjectMother.sleep(Duration.ofSeconds(5));
		// return null;
		// }).when(taskExecutor).execute(any(), any());
		// final TaskConsumerTest taskConsumer = new
		// TaskConsumerTest(taskContextProvider, taskExecutor);
		// taskConsumer.consume();
	}

	private void scheduleTask() {
		scheduleTask(ObjectMother.taskType());
	}

	private void scheduleTask(final VersionedName taskType) {
		ObjectMother.makeDecider(to -> to.scheduleTask(taskType))
				.when(this.workflowfStartedHandler).onWorkflowStarted(any(), any());
		this.workflow.start();
		this.consumer.consume();
	}

	private void startTimer(final String timerName, final String input, final int timeoutInSeconds) {
		ObjectMother.makeDecider(to -> to.startTimer(timerName, input, Duration.ofSeconds(timeoutInSeconds)))
				.when(this.workflowfStartedHandler).onWorkflowStarted(any(), any());
		this.workflow.start();
		this.consumer.consume();
	}

}
