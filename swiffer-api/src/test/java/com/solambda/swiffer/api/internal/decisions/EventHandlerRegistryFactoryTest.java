package com.solambda.swiffer.api.internal.decisions;

import static com.amazonaws.services.simpleworkflow.model.EventType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import com.amazonaws.services.simpleworkflow.model.CancelWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.ContinueAsNewWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionFailedCause;
import com.solambda.swiffer.api.*;
import com.solambda.swiffer.api.exceptions.WorkflowTemplateException;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.identifier.ActivityName;
import com.solambda.swiffer.api.internal.context.identifier.MarkerName;
import com.solambda.swiffer.api.internal.context.identifier.SignalName;
import com.solambda.swiffer.api.internal.context.identifier.TimerName;
import com.solambda.swiffer.api.internal.context.identifier.WorkflowName;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.mapper.JacksonDataMapper;
import com.solambda.swiffer.api.retry.RetryControl;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class EventHandlerRegistryFactoryTest {

	private static final String SIGNAL1 = "signal1";
	private static final String TIMER1 = "timer1";
	private static final String MARKER1 = "marker1";
	private static final VersionedName WORKFLOW = new VersionedName("workflow1", "1");
	private static final VersionedName ACTIVITY1 = new VersionedName("activity1", "1");
	private static final String CHILD_WORKFLOW_NAME = "child";
	private static final String CHILD_WORKFLOW_VERSION = "20";
	private final DataMapper dataMapper = new JacksonDataMapper();
	private final RetryPolicy retryPolicy = mock(RetryPolicy.class);

	@ActivityType(name = "activity1", version = "1")
	public static interface ActivityDef {

	}

	private static class Template1 {

		@OnWorkflowStarted
		public void onWorkflowStarted() {

		}

		@OnActivityCompleted(value = ActivityDef.class)
		public void onActivityCompleted() {

		}

		@OnActivityFailed(activity = ActivityDef.class)
		public void onActivityFailed() {

		}

		@OnTimerFired(value = TIMER1)
		public void onTimerFired() {

		}

		@OnSignalReceived(value = SIGNAL1)
		public void onSignalReceived() {

		}

		@OnMarkerRecorded(MARKER1)
		public void onMarkerRecorded() {}

		@OnRecordMarkerFailed(MARKER1)
		public void onRecordMarkerFailed() {}

		@OnChildWorkflowCanceled(ChildWorkflow.class)
		public void onChildWorkflowCanceled() {}

		@OnChildWorkflowCompleted(ChildWorkflow.class)
		public void onChildWorkflowCompleted() {}

		@OnChildWorkflowFailed(ChildWorkflow.class)
		public void onChildWorkflowFailed() {}

		@OnChildWorkflowStarted(ChildWorkflow.class)
		public void onChildWorkflowStarted() {}

		@OnChildWorkflowTerminated(ChildWorkflow.class)
		public void onChildWorkflowTerminated() {}

		@OnChildWorkflowTimedOut(ChildWorkflow.class)
		public void onChildWorkflowTimedOut() {}

		@OnStartChildWorkflowFailed(ChildWorkflow.class)
		public void onStartChildWorkflowFailed() {}

		@OnWorkflowCancelRequested
		public void onWorkflowCancelRequested() {}
	}

	private static class TemplateWithTwoHandlersOfTheSameType {

		@OnWorkflowStarted
		public void onWorkflowStarted() {

		}

		@OnWorkflowStarted
		public void onWorkflowStarted2() {

		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@WorkflowType(name = CHILD_WORKFLOW_NAME, version = CHILD_WORKFLOW_VERSION)
	public @interface ChildWorkflow {}

	@ChildWorkflow
	private static class ChildWorkflowTemplate {}

	@Test
	public void handlersAreRetrieved() {
		final EventHandlerRegistryFactory factory = new EventHandlerRegistryFactory(WORKFLOW, dataMapper, retryPolicy);
		final EventHandlerRegistry registry = factory.build(new Template1());
		final ActivityName activityName = new ActivityName(ACTIVITY1);
		assertThat(registry.get(new EventHandlerType(ActivityTaskCompleted, activityName))).isNotNull();
		assertThat(registry.get(new EventHandlerType(ActivityTaskFailed, activityName))).isNotNull();
		assertThat(registry.get(new EventHandlerType(TimerFired, new TimerName(TIMER1)))).isNotNull();
        assertThat(registry.get(new EventHandlerType(MarkerRecorded, new MarkerName(MARKER1)))).isNotNull();
        assertThat(registry.get(new EventHandlerType(RecordMarkerFailed, new MarkerName(MARKER1)))).isNotNull();
		final SignalName signalName = new SignalName(SIGNAL1);
		assertThat(registry.get(new EventHandlerType(WorkflowExecutionSignaled, signalName))).isNotNull();
		final WorkflowName workflowName = new WorkflowName(WORKFLOW);
		assertThat(registry.get(new EventHandlerType(WorkflowExecutionStarted, workflowName))).isNotNull();

		WorkflowName childWorkflow = new WorkflowName(CHILD_WORKFLOW_NAME, CHILD_WORKFLOW_VERSION);
		assertThat(registry.get(new EventHandlerType(ChildWorkflowExecutionCanceled, childWorkflow))).isNotNull();
		assertThat(registry.get(new EventHandlerType(ChildWorkflowExecutionCompleted, childWorkflow))).isNotNull();
		assertThat(registry.get(new EventHandlerType(ChildWorkflowExecutionFailed, childWorkflow))).isNotNull();
		assertThat(registry.get(new EventHandlerType(ChildWorkflowExecutionStarted, childWorkflow))).isNotNull();
		assertThat(registry.get(new EventHandlerType(ChildWorkflowExecutionTerminated, childWorkflow))).isNotNull();
		assertThat(registry.get(new EventHandlerType(ChildWorkflowExecutionTimedOut, childWorkflow))).isNotNull();
		assertThat(registry.get(new EventHandlerType(StartChildWorkflowExecutionFailed, childWorkflow))).isNotNull();

		assertThat(registry.get(new EventHandlerType(WorkflowExecutionCancelRequested, workflowName))).isNotNull();
	}

	@Test
	public void cannotHaveTwoHandlersForTheSameType() {
		final EventHandlerRegistryFactory factory = new EventHandlerRegistryFactory(WORKFLOW, dataMapper, retryPolicy);
		assertThatExceptionOfType(WorkflowTemplateException.class)
				.isThrownBy(() -> factory.build(new TemplateWithTwoHandlersOfTheSameType()))
				.withMessageContaining("more than one handler of");
	}

	@Test
	public void build_RegistryHasDefaultHandlers() throws Exception {
		EventHandlerRegistryFactory factory = new EventHandlerRegistryFactory(WORKFLOW, dataMapper, retryPolicy);

		EventHandlerRegistry registry = factory.build(new Template1());

		assertThat(registry.getDefaultCancelWorkflowExecutionFailedHandler(null)).isNull();
		assertThat(registry.getDefaultCancelWorkflowExecutionFailedHandler("any")).isNull();
		assertThat(registry.getDefaultCompleteWorkflowExecutionFailedHandler(null)).isNull();
		assertThat(registry.getDefaultCompleteWorkflowExecutionFailedHandler("any")).isNull();
		assertThat(registry.getDefaultContinueAsNewWorkflowExecutionFailedHandler(null)).isNull();
		assertThat(registry.getDefaultContinueAsNewWorkflowExecutionFailedHandler("any")).isNull();
		assertThat(registry.getDefaultFailWorkflowExecutionFailedHandler(null)).isNull();
		assertThat(registry.getDefaultFailWorkflowExecutionFailedHandler("any")).isNull();
		assertThat(registry.getDefaultRetryTimerFiredHandler("any")).isNull();

		assertThat(registry.getDefaultRetryTimerFiredHandler(RetryControl.RETRY_TIMER + "someActivity")).isNotNull();
		assertThat(registry.getDefaultFailedActivityHandler()).isNotNull();
		assertThat(registry.getDefaultTimedOutActivityHandler()).isNotNull();

		assertThat(registry.getDefaultCancelWorkflowExecutionFailedHandler(CancelWorkflowExecutionFailedCause.UNHANDLED_DECISION.name())).isNotNull();
		assertThat(registry.getDefaultCompleteWorkflowExecutionFailedHandler(CompleteWorkflowExecutionFailedCause.UNHANDLED_DECISION.name())).isNotNull();
		assertThat(registry.getDefaultContinueAsNewWorkflowExecutionFailedHandler(ContinueAsNewWorkflowExecutionFailedCause.UNHANDLED_DECISION.name())).isNotNull();
		assertThat(registry.getDefaultFailWorkflowExecutionFailedHandler(FailWorkflowExecutionFailedCause.UNHANDLED_DECISION.name())).isNotNull();
	}
}
