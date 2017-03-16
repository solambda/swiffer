package com.solambda.swiffer.api.internal.decisions;

import static com.amazonaws.services.simpleworkflow.model.EventType.ActivityTaskCompleted;
import static com.amazonaws.services.simpleworkflow.model.EventType.ActivityTaskFailed;
import static com.amazonaws.services.simpleworkflow.model.EventType.MarkerRecorded;
import static com.amazonaws.services.simpleworkflow.model.EventType.RecordMarkerFailed;
import static com.amazonaws.services.simpleworkflow.model.EventType.TimerFired;
import static com.amazonaws.services.simpleworkflow.model.EventType.WorkflowExecutionSignaled;
import static com.amazonaws.services.simpleworkflow.model.EventType.WorkflowExecutionStarted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.OnActivityCompleted;
import com.solambda.swiffer.api.OnActivityFailed;
import com.solambda.swiffer.api.OnMarkerRecorded;
import com.solambda.swiffer.api.OnRecordMarkerFailed;
import com.solambda.swiffer.api.OnSignalReceived;
import com.solambda.swiffer.api.OnTimerFired;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.api.exceptions.WorkflowTemplateException;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.identifier.ActivityName;
import com.solambda.swiffer.api.internal.context.identifier.MarkerName;
import com.solambda.swiffer.api.internal.context.identifier.SignalName;
import com.solambda.swiffer.api.internal.context.identifier.TimerName;
import com.solambda.swiffer.api.internal.context.identifier.WorkflowName;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.mapper.JacksonDataMapper;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class EventHandlerRegistryFactoryTest {

	private static final String SIGNAL1 = "signal1";
	private static final String TIMER1 = "timer1";
	private static final String MARKER1 = "marker1";
	private static final VersionedName WORKFLOW = new VersionedName("workflow1", "1");
	private static final VersionedName ACTIVITY1 = new VersionedName("activity1", "1");
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
		public void onMarkerRecorded(){}

		@OnRecordMarkerFailed(MARKER1)
		public void onRecordMarkerFailed(){}
	}

	private static class TemplateWithTwoHandlersOfTheSameType {

		@OnWorkflowStarted
		public void onWorkflowStarted() {

		}

		@OnWorkflowStarted
		public void onWorkflowStarted2() {

		}

	}

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
	}

	@Test
	public void cannotHaveTwoHandlersForTheSameType() {
		final EventHandlerRegistryFactory factory = new EventHandlerRegistryFactory(WORKFLOW, dataMapper, retryPolicy);
		assertThatExceptionOfType(WorkflowTemplateException.class)
				.isThrownBy(() -> factory.build(new TemplateWithTwoHandlersOfTheSameType()))
				.withMessageContaining("more than one handler of");
	}
}
