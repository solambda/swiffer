package com.solambda.swiffer.api.internal.activities;

import static com.solambda.swiffer.test.Tests.returnAfterDelay;
import static com.solambda.swiffer.test.Tests.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.PollForActivityTaskRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskFailedRequest;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.Executor;
import com.solambda.swiffer.api.Swiffer;
import com.solambda.swiffer.api.Worker;
import com.solambda.swiffer.api.exceptions.TaskContextPollingException;
import com.solambda.swiffer.api.internal.activities.WorkerImplTest.Definitions.FailingActivity;
import com.solambda.swiffer.api.internal.activities.WorkerImplTest.Definitions.NoArgumentActivity;
import com.solambda.swiffer.api.internal.activities.WorkerImplTest.Definitions.NoReturnValueActivity;
import com.solambda.swiffer.api.internal.activities.WorkerImplTest.Definitions.ToUpperCase;
import com.solambda.swiffer.test.Tests;

public class WorkerImplTest {
	private static final String TASK_TOKEN = "token";
	private static final String ACTIVITY_NAME = "activity1";
	private static final String ACTIVITY_VERSION = "1";
	private static final String NO_EXECUTOR_ACTIVITY_NAME = "noExecutorActivity";
	private static final String NO_ARGUMENT_ACTIVITY_NAME = "noArgumentActivity";
	private static final String NO_RETURN_VALUE_ACTIVITY_NAME = "noReturnValueActivity";
	private static final String FAILING_ACTIVITY_NAME = "failingActivity";
	private static final String ACTIVITY_ID = "activityId";

	private AmazonSimpleWorkflow swf;
	private Swiffer swiffer;
	private TestExecutors executors;

	@Before
	public void setup() {
		this.swf = mock(AmazonSimpleWorkflow.class);
		this.swiffer = new Swiffer(this.swf, Tests.DOMAIN);
		this.executors = spy(new TestExecutors());
	}

	public static class Definitions {
		@ActivityType(name = ACTIVITY_NAME, version = ACTIVITY_VERSION)
		public static interface ToUpperCase {

		}

		@ActivityType(name = NO_EXECUTOR_ACTIVITY_NAME, version = ACTIVITY_VERSION)
		public static interface NoExecutorActivity {

		}

		@ActivityType(name = NO_ARGUMENT_ACTIVITY_NAME, version = ACTIVITY_VERSION)
		public static interface NoArgumentActivity {

		}

		@ActivityType(name = NO_RETURN_VALUE_ACTIVITY_NAME, version = ACTIVITY_VERSION)
		public static interface NoReturnValueActivity {

		}

		@ActivityType(name = FAILING_ACTIVITY_NAME, version = ACTIVITY_VERSION)
		public static interface FailingActivity {

		}
	}

	public static class TestExecutors {

		@Executor(activity = ToUpperCase.class)
		public String doActivity1(final String input) {
			return input.toUpperCase();
		}

		@Executor(activity = NoArgumentActivity.class)
		public String noArgActivity() {
			return "OK";
		}

		@Executor(activity = NoReturnValueActivity.class)
		public void noReturnValueActivity() {
		}

		@Executor(activity = FailingActivity.class)
		public void failingActivity() {
			throw new IllegalStateException("the activity has failed!");
		}

	}

	private Worker createWorker() {
		return this.swiffer.newWorkerBuilder()
				.identity("worker-test")
				.taskList("test-task-list")
				.executors(this.executors)
				.build();
	}

	private void anActivityTaskInTheTaskList(final String activityName) {
		when(this.swf.pollForActivityTask(any(PollForActivityTaskRequest.class)))
				.thenReturn(new ActivityTask()
						.withTaskToken(TASK_TOKEN)
						.withActivityId(ACTIVITY_ID)
						.withInput("\"some input text\"")
						.withActivityType(
								new com.amazonaws.services.simpleworkflow.model.ActivityType()
										.withName(activityName)
										.withVersion(ACTIVITY_VERSION)));
	}

	@Test
	public void workerInvokePollForActivityTask() throws Exception {
		// GIVEN
		final Worker worker = createWorker();
		anActivityTaskInTheTaskList(ACTIVITY_NAME);

		// WHEN
		worker.start();
		sleep(Duration.ofMillis(100));
		final ArgumentCaptor<PollForActivityTaskRequest> captor = ArgumentCaptor
				.forClass(PollForActivityTaskRequest.class);
		// THEN
		verify(this.swf, atLeastOnce()).pollForActivityTask(captor.capture());
		final PollForActivityTaskRequest request = captor.getValue();
		assertThat(request.getTaskList().getName()).isEqualTo("test-task-list");
		assertThat(request.getIdentity()).isEqualTo("worker-test");
		assertThat(request.getDomain()).isEqualTo(Tests.DOMAIN);
	}

	@Test
	public void workerInvokePollForActivityTaskEagerly() throws Exception {
		// GIVEN
		final Worker worker = createWorker();
		// polling takes 200 ms, executor takes 1 sec each, during 1 sec there
		// are 4 polling operations
		when(this.swf.pollForActivityTask(any(PollForActivityTaskRequest.class)))
				.then(returnAfterDelay(new ActivityTask().withTaskToken(TASK_TOKEN), Duration.ofMillis(200)));
		// WHEN
		worker.start();
		sleep(Duration.ofMillis(1000));
		worker.stop();

		// THEN
		final ArgumentCaptor<PollForActivityTaskRequest> captor = ArgumentCaptor
				.forClass(PollForActivityTaskRequest.class);
		verify(this.swf, times(5)).pollForActivityTask(captor.capture());

	}

	@Test
	public void workerInvokeRespondTaskFailedIfThereIsNoExecutorForTheActivityType() throws Exception {
		// GIVEN
		anActivityTaskInTheTaskList(NO_EXECUTOR_ACTIVITY_NAME);

		// WHEN
		final WorkerImpl worker = (WorkerImpl) createWorker();
		worker.pollAndExecuteTask();

		// THEN
		theMethodRespondActivityTaskFailedIsCalledWithCorrectArguments();
	}

	private void theMethodRespondActivityTaskFailedIsCalledWithCorrectArguments() {
		final ArgumentCaptor<RespondActivityTaskFailedRequest> captor = ArgumentCaptor
				.forClass(RespondActivityTaskFailedRequest.class);
		verify(this.swf).respondActivityTaskFailed(captor.capture());
		final RespondActivityTaskFailedRequest request = captor.getValue();
		assertThat(request.getDetails()).isEqualTo(null);
		assertThat(request.getReason())
				.isEqualTo("no executor defined "
						+ "for activity {name=\"" + NO_EXECUTOR_ACTIVITY_NAME
						+ "\",version=\"1\"");
		assertThat(request.getTaskToken()).isEqualTo(TASK_TOKEN);
	}

	@Test
	public void theAppropriateExecutorMethodIsInvoked() throws Exception {
		// GIVEN
		anActivityTaskInTheTaskList(NO_ARGUMENT_ACTIVITY_NAME);

		// WHEN
		final WorkerImpl worker = (WorkerImpl) createWorker();
		worker.pollAndExecuteTask();

		// THEN
		verify(this.executors, times(1)).noArgActivity();
		verify(this.executors, never()).doActivity1(anyString());
	}

	@Test
	public void workerInvokeRespondTaskCompletedOnSuccess() throws Exception {
		// GIVEN
		anActivityTaskInTheTaskList(NO_ARGUMENT_ACTIVITY_NAME);
		// WHEN
		final WorkerImpl worker = (WorkerImpl) createWorker();
		worker.pollAndExecuteTask();

		// THEN
		final ArgumentCaptor<RespondActivityTaskCompletedRequest> captor = ArgumentCaptor
				.forClass(RespondActivityTaskCompletedRequest.class);
		verify(this.swf).respondActivityTaskCompleted(captor.capture());
		final RespondActivityTaskCompletedRequest request = captor.getValue();

		assertThat(request.getResult()).isEqualTo("\"OK\"");
		assertThat(request.getTaskToken()).isEqualTo(TASK_TOKEN);
	}

	@Test
	public void outputIsPassedToRespondTaskCompletedWithExecutorReturnValue() throws TaskContextPollingException {
		// GIVEN
		anActivityTaskInTheTaskList(NO_ARGUMENT_ACTIVITY_NAME);
		// WHEN
		final WorkerImpl worker = (WorkerImpl) createWorker();
		worker.pollAndExecuteTask();

		// THEN
		final ArgumentCaptor<RespondActivityTaskCompletedRequest> captor = ArgumentCaptor
				.forClass(RespondActivityTaskCompletedRequest.class);
		verify(this.swf).respondActivityTaskCompleted(captor.capture());
		final RespondActivityTaskCompletedRequest request = captor.getValue();

		assertThat(request.getResult()).isEqualTo("\"OK\"");
		assertThat(request.getTaskToken()).isEqualTo(TASK_TOKEN);
	}

	@Test
	public void noOutputIsPassedToRespondTaskCompletedForExecutorWithVoidReturnValue()
			throws TaskContextPollingException {
		// GIVEN
		anActivityTaskInTheTaskList(NO_RETURN_VALUE_ACTIVITY_NAME);
		// WHEN
		final WorkerImpl worker = (WorkerImpl) createWorker();
		worker.pollAndExecuteTask();

		// THEN
		final ArgumentCaptor<RespondActivityTaskCompletedRequest> captor = ArgumentCaptor
				.forClass(RespondActivityTaskCompletedRequest.class);
		verify(this.swf).respondActivityTaskCompleted(captor.capture());
		final RespondActivityTaskCompletedRequest request = captor.getValue();

		assertThat(request.getResult()).isEqualTo(null);
		assertThat(request.getTaskToken()).isEqualTo(TASK_TOKEN);
	}

	@Test
	public void workerInvokeRespondTaskFailedOnFailure() throws Exception {
		anActivityTaskInTheTaskList(FAILING_ACTIVITY_NAME);
		// WHEN
		final WorkerImpl worker = (WorkerImpl) createWorker();
		worker.pollAndExecuteTask();

		// THEN
		final ArgumentCaptor<RespondActivityTaskFailedRequest> captor = ArgumentCaptor
				.forClass(RespondActivityTaskFailedRequest.class);
		verify(this.swf).respondActivityTaskFailed(captor.capture());
		final RespondActivityTaskFailedRequest request = captor.getValue();

		assertThat(request.getReason()).isEqualTo("Task execution failed");
		assertThat(request.getDetails())
				.contains("Execution of activity")
				.contains("['failingActivity','1']")
				.contains("id=" + ACTIVITY_ID + " failed!");
		assertThat(request.getTaskToken()).isEqualTo(TASK_TOKEN);
	}

	@Test
	public void executorIsInvokedWithInput() throws Exception {
		// GIVEN
		anActivityTaskInTheTaskList(ACTIVITY_NAME);

		// WHEN
		final WorkerImpl worker = (WorkerImpl) createWorker();
		worker.pollAndExecuteTask();

		// THEN
		verify(this.executors, times(1)).doActivity1("some input text");

		final ArgumentCaptor<RespondActivityTaskCompletedRequest> captor = ArgumentCaptor
				.forClass(RespondActivityTaskCompletedRequest.class);
		verify(this.swf).respondActivityTaskCompleted(captor.capture());
		final RespondActivityTaskCompletedRequest request = captor.getValue();

		assertThat(request.getResult()).isEqualTo("\"SOME INPUT TEXT\"");
		assertThat(request.getTaskToken()).isEqualTo(TASK_TOKEN);
	}

	@Test
	@Ignore
	public void workerInvokeRespondTaskFailedIfTheExecutorInputsCannotBeSet() throws Exception {
		fail("not yet implemented");
	}

	@Test
	@Ignore("not yet implemetned")
	public void workerInvokeTaskExecutionAsynchronously() {
		fail("not yet implemented");

		// poll, execute long running activity, and verify a polling occur
		// during the activity execution
	}

	@Test
	@Ignore("not yet implemetned")
	public void stopPreventFurtherPollingToOccur() {
		fail("not yet implemented");

		// start polling, let it polls one or two tasks, stop during a polling,
		// and verify no polling occur any more.
	}

	@Test
	@Ignore("not yet implemetned")
	public void stopImmediatelyInterruptsTheCurrentPolling() {
		fail("not yet implemented");

		// start polling, waiting for a new task to be available, stop, and
		// assert it stop immediately
		// it this possible ?
	}

	@Test
	@Ignore("not yet implemetned")
	public void stopDoNotInterruptTaskExecutionToComplete() {
		fail("not yet implemented");

		// poll, execute long running activity, stop polling, and verify the
		// activity execution completes
	}

	@Test
	@Ignore("not yet implemetned")
	public void exceptionDuringPollingStopTheWorker() {
		fail("not yet implemented");

		// how to detect that in production ?

	}

	@Test
	@Ignore("not yet implemetned")
	public void testBehaviorOfTimedOutActivityThatFinishAnyway() {
		fail("not yet implemented: should be done in integration testing");
	}

	@Test
	@Ignore("not yet implemetned")
	public void aWorkerCanPollSeveralTaksListsInParallel() {
		fail("not yet implemented");
	}

	@Test
	@Ignore("not yet implemetned")
	public void activityTypesAreRegisteredOnBuildTime() {
		fail("not yet implemented");
	}
}
