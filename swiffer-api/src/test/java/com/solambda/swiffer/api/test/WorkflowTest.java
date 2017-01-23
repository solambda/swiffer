package com.solambda.swiffer.api.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.solambda.swiffer.api.model.Workflow;
import com.solambda.swiffer.api.model.WorkflowBuilder;
import com.solambda.swiffer.api.model.decider.Decider;
import com.solambda.swiffer.api.model.decider.impl.DecisionContextProviderImpl;

/**
 * TODO: - test output retrieval of complete, failed, and canceled
 */
public class WorkflowTest {

	public static class AnUnregisteredWorkflow {
		@Test(expected = IllegalStateException.class)
		public void cannotBeManaged() throws Exception {
			final WorkflowBuilder builder = new WorkflowBuilder()
					.client(ObjectMother.client())
					.domain(ObjectMother.domainName())
					.type(ObjectMother.unregisteredWorkflowType())
					.id("my-workflow-id");
			builder.build();
		}
	}

	public static class ANotStartedWorkflow {
		private WorkflowBuilder builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.registeredWorkflowType())
				.id("not-started");
		private boolean started = false;
		private Workflow workflow = builder.build();

		@Before
		public void resetStarted() {
			started = false;
			workflow = builder.build();
		}

		@After
		public void terminatePotentiallyStarted() {
			if (started) {
				workflow.terminate();
			}
		}

		@Test
		public void isNotStarted() throws Exception {
			assertThat(workflow.isStarted()).isFalse();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotCheckIfClosed() throws Exception {
			assertThat(workflow.isClosed()).isFalse();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotCheckIfCanceled() throws Exception {
			workflow.isCanceled();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotCheckIfTerminated() throws Exception {
			workflow.isTerminated();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotCheckIfTimedOut() throws Exception {
			workflow.isTimedOut();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotCheckIfComplete() throws Exception {
			workflow.isComplete();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotCheckIfContinued() throws Exception {
			workflow.isContinued();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotCheckIfFailed() throws Exception {
			workflow.isFailed();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeTerminated() throws Exception {
			workflow.terminate();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeCanceled() throws Exception {
			workflow.requestCancel();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeSignaled() throws Exception {
			workflow.signal("signal");
		}

		@Test
		public void canBeStarted() throws Exception {
			started = true;
			workflow.start();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotAwaitClose() {
			workflow.awaitClose();
		}

	}

	public static class AStartedWorkflow {
		WorkflowBuilder builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.registeredWorkflowType())
				.id("started");
		Workflow workflow = builder.build();

		@Before
		public void start() {
			workflow.start();
		}

		@After
		public void terminateWorkflow() {
			try {
				workflow.terminate();
			} catch (final Exception e) {
			}
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeStarted() throws Exception {
			workflow.start();
		}

		@Test
		public void isStarted() throws Exception {
			assertThat(workflow.isStarted()).isTrue();
		}

		@Test
		public void isNotClosed() throws Exception {
			assertThat(workflow.isClosed()).isFalse();
		}

		@Test
		public void isNotCanceled() throws Exception {
			assertThat(workflow.isCanceled()).isFalse();
		}

		@Test
		public void isNotTerminated() throws Exception {
			assertThat(workflow.isTerminated()).isFalse();
		}

		@Test
		public void isNotTimedOut() throws Exception {
			assertThat(workflow.isTimedOut()).isFalse();
		}

		@Test
		public void isNotComplete() throws Exception {
			assertThat(workflow.isComplete()).isFalse();
		}

		@Test
		public void isNotContinued() throws Exception {
			assertThat(workflow.isContinued()).isFalse();
		}

		@Test
		public void isNotFailed() throws Exception {
			assertThat(workflow.isFailed()).isFalse();
		}

		@Test
		public void canBeCancelled() throws Exception {
			workflow.requestCancel();
		}

		@Test
		public void canBeSignaled() throws Exception {
			workflow.signal("signal");
		}

		@Test
		public void canBeTerminated() throws Exception {
			workflow.terminate();
		}

		@Test
		public void canAwaitClose() {
			completeWorkflow();
			workflow.awaitClose();
		}

	}

	public static class ATerminatedWorkflow {
		WorkflowBuilder builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.registeredWorkflowType())
				.id("terminated");
		Workflow workflow = builder.build();

		@Before
		public void start() {
			workflow.start();
			workflow.terminate();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeStarted() throws Exception {
			workflow.start();
		}

		@Test
		public void isNotStarted() throws Exception {
			assertThat(workflow.isStarted()).isFalse();
		}

		@Test
		public void isClosed() throws Exception {
			assertThat(workflow.isClosed()).isTrue();
		}

		@Test
		public void isNotCanceled() throws Exception {
			assertThat(workflow.isCanceled()).isFalse();
		}

		@Test
		public void isTerminated() throws Exception {
			assertThat(workflow.isTerminated()).isTrue();
		}

		@Test
		public void isNotTimedOut() throws Exception {
			assertThat(workflow.isTimedOut()).isFalse();
		}

		@Test
		public void isNotComplete() throws Exception {
			assertThat(workflow.isComplete()).isFalse();
		}

		@Test
		public void isNotContinued() throws Exception {
			assertThat(workflow.isContinued()).isFalse();
		}

		@Test
		public void isNotFailed() throws Exception {
			assertThat(workflow.isFailed()).isFalse();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeCancelled() throws Exception {
			workflow.requestCancel();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeSignaled() throws Exception {
			workflow.signal("signal");
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeTerminated() throws Exception {
			workflow.terminate();
		}

		@Test
		public void canAwaitClose() {
			workflow.awaitClose();
		}

	}

	public static class ACanceledWorkflow {
		private static WorkflowBuilder builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.registeredWorkflowType())
				.id("canceled");
		private static Workflow workflow = builder.build();

		@BeforeClass
		public static void createCanceledWorkflow() {
			workflow.start();
			workflow.requestCancel();
			cancelWorkflow();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeStarted() throws Exception {
			workflow.start();
		}

		@Test
		public void isNotStarted() throws Exception {
			assertThat(workflow.isStarted()).isFalse();
		}

		@Test
		public void isClosed() throws Exception {
			assertThat(workflow.isClosed()).isTrue();
		}

		@Test
		public void isCanceled() throws Exception {
			assertThat(workflow.isCanceled()).isTrue();
		}

		@Test
		public void isNotTerminated() throws Exception {
			assertThat(workflow.isTerminated()).isFalse();
		}

		@Test
		public void isNotTimedOut() throws Exception {
			assertThat(workflow.isTimedOut()).isFalse();
		}

		@Test
		public void isNotComplete() throws Exception {
			assertThat(workflow.isComplete()).isFalse();
		}

		@Test
		public void isNotContinued() throws Exception {
			assertThat(workflow.isContinued()).isFalse();
		}

		@Test
		public void isNotFailed() throws Exception {
			assertThat(workflow.isFailed()).isFalse();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeCancelled() throws Exception {
			workflow.requestCancel();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeSignaled() throws Exception {
			workflow.signal("signal");
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeTerminated() throws Exception {
			workflow.terminate();
		}

		@Test
		public void canAwaitClose() {
			workflow.awaitClose();
		}
	}

	public static class ACompleteWorkflow {
		private static WorkflowBuilder builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.registeredWorkflowType())
				.id("complete");
		private static Workflow workflow = builder.build();

		@BeforeClass
		public static void createCompleteWorkflow() {
			workflow.start();
			completeWorkflow();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeStarted() throws Exception {
			workflow.start();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeCancelled() throws Exception {
			workflow.requestCancel();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeSignaled() throws Exception {
			workflow.signal("signal");
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeTerminated() throws Exception {
			workflow.terminate();
		}

		@Test
		public void isNotStarted() throws Exception {
			assertThat(workflow.isStarted()).isFalse();
		}

		@Test
		public void isClosed() throws Exception {
			assertThat(workflow.isClosed()).isTrue();
		}

		@Test
		public void isNotCanceled() throws Exception {
			assertThat(workflow.isCanceled()).isFalse();
		}

		@Test
		public void isNotTerminated() throws Exception {
			assertThat(workflow.isTerminated()).isFalse();
		}

		@Test
		public void isNotTimedOut() throws Exception {
			assertThat(workflow.isTimedOut()).isFalse();
		}

		@Test
		public void isComplete() throws Exception {
			assertThat(workflow.isComplete()).isTrue();
		}

		@Test
		public void isNotContinued() throws Exception {
			assertThat(workflow.isContinued()).isFalse();
		}

		@Test
		public void isNotFailed() throws Exception {
			assertThat(workflow.isFailed()).isFalse();
		}

		@Test
		public void canAwaitClose() {
			workflow.awaitClose();
		}

	}

	public static class AFailedWorkflow {
		private static WorkflowBuilder builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.registeredWorkflowType())
				.id("failed");
		private static Workflow workflow = builder.build();

		@BeforeClass
		public static void createCompleteWorkflow() {
			workflow.start();
			failWorkflow();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeStarted() throws Exception {
			workflow.start();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeCancelled() throws Exception {
			workflow.requestCancel();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeSignaled() throws Exception {
			workflow.signal("signal");
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeTerminated() throws Exception {
			workflow.terminate();
		}

		@Test
		public void isNotStarted() throws Exception {
			assertThat(workflow.isStarted()).isFalse();
		}

		@Test
		public void isClosed() throws Exception {
			assertThat(workflow.isClosed()).isTrue();
		}

		@Test
		public void isNotCanceled() throws Exception {
			assertThat(workflow.isCanceled()).isFalse();
		}

		@Test
		public void isNotTerminated() throws Exception {
			assertThat(workflow.isTerminated()).isFalse();
		}

		@Test
		public void isNotTimedOut() throws Exception {
			assertThat(workflow.isTimedOut()).isFalse();
		}

		@Test
		public void isNotComplete() throws Exception {
			assertThat(workflow.isComplete()).isFalse();
		}

		@Test
		public void isNotContinued() throws Exception {
			assertThat(workflow.isContinued()).isFalse();
		}

		@Test
		public void isFailed() throws Exception {
			assertThat(workflow.isFailed()).isTrue();
		}

		@Test
		public void canAwaitClose() {
			workflow.awaitClose();
		}

	}

	public static class ATimedOutWorkflow {
		private static WorkflowBuilder builder = new WorkflowBuilder()
				.client(ObjectMother.client())
				.domain(ObjectMother.domainName())
				.type(ObjectMother.smallTimeoutWorkflowType())
				.id("timed-out");
		private static Workflow workflow = builder.build();

		@BeforeClass
		public static void start() {
			// (OPTIM: get a timeout workflw execution or )
			// create a new workflow and wait it is timedout
			workflow.start();
			workflow.awaitClose();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeStarted() throws Exception {
			workflow.start();
		}

		@Test
		public void isNotStarted() throws Exception {
			assertThat(workflow.isStarted()).isFalse();
		}

		@Test
		public void isClosed() throws Exception {
			assertThat(workflow.isClosed()).isTrue();
		}

		@Test
		public void isNotCanceled() throws Exception {
			assertThat(workflow.isCanceled()).isFalse();
		}

		@Test
		public void isNotTerminated() throws Exception {
			assertThat(workflow.isTerminated()).isFalse();
		}

		@Test
		public void isTimedOut() throws Exception {
			assertThat(workflow.isTimedOut()).isTrue();
		}

		@Test
		public void isNotComplete() throws Exception {
			assertThat(workflow.isComplete()).isFalse();
		}

		@Test
		public void isNotContinued() throws Exception {
			assertThat(workflow.isContinued()).isFalse();
		}

		@Test
		public void isNotFailed() throws Exception {
			assertThat(workflow.isFailed()).isFalse();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeCancelled() throws Exception {
			workflow.requestCancel();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeSignaled() throws Exception {
			workflow.signal("signal");
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeTerminated() throws Exception {
			workflow.terminate();
		}

		@Test
		public void canAwaitClose() {
			workflow.awaitClose();
		}

	}

	public static void completeWorkflow() {
		invokeDecider(ObjectMother.completeWorkflowDecider());
	}

	public static void cancelWorkflow() {
		invokeDecider(ObjectMother.cancelWorkflowDecider());
	}

	public static void failWorkflow() {
		invokeDecider(ObjectMother.failWorkflowDecider());
	}

	private static void invokeDecider(final Decider decider) {
		final DecisionContextProviderImpl provider = new DecisionContextProviderImpl(ObjectMother.client(),
				ObjectMother.domainName(), null,
				WorkflowTest.class.getName());
		final WorkflowConsumerTest iterator = new WorkflowConsumerTest(decider, provider);
		iterator.consume();
	}

}
