package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.Test;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.*;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.identifier.ActivityName;
import com.solambda.swiffer.api.internal.context.identifier.MarkerName;
import com.solambda.swiffer.api.internal.context.identifier.SignalName;
import com.solambda.swiffer.api.internal.context.identifier.TimerName;
import com.solambda.swiffer.api.internal.context.identifier.WorkflowName;
import com.solambda.swiffer.api.retry.RetryControl;

public class EventHandlerTypeFactoryTest {
	private static final String SIGNAL1 = "signal1";
	private static final String TIMER1 = "timer1";
	private static final String MARKER1 = "marker1";

	private static final VersionedName WORKFLOW_TYPE = new VersionedName("workflow1", "1");
    private static final WorkflowName CHILD_WORKFLOW_NAME = new WorkflowName("child-wf", "1");

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

		@OnWorkflowStarted
		@OnActivityCompleted(value = ActivityDef.class)
		public void failWithDoubleAnnotations() {

		}

        @OnMarkerRecorded(MARKER1)
        public void onMarkerRecorded(){}

        @OnRecordMarkerFailed(MARKER1)
        public void onRecordMarkerFailed(){}

		@SuppressWarnings("unused")
		public void notAnnotatedMethod() {

		}

        @OnTimerFired(RetryControl.RETRY_TIMER + "some_other_value")
        public void onRetryTimerFired(){

        }

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

    @Retention(RetentionPolicy.RUNTIME)
    @WorkflowType(name = "child-wf", version = "1")
    public @interface ChildWorkflow {}

    @ChildWorkflow
    private static class ChildWorkflowTemplate {}

	@Test
	public void cannotDeclareMoreThan2HandlerAnnotation() throws Exception {
		final EventHandlerTypeFactory factory = createFactory();
		final Method method = Template1.class.getMethod("failWithDoubleAnnotations");
		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> factory.create(method))
				.withMessageContaining("more than one event");

	}

	@Test
	public void notAnnotatedMethod_shouldreturnnull() throws Exception {
		final EventHandlerTypeFactory factory = createFactory();
		final Method method = Template1.class.getMethod("notAnnotatedMethod");
		final EventHandlerType type = factory.create(method);
		assertThat(type).isNull();
	}

	private EventHandlerTypeFactory createFactory() {
		final EventHandlerTypeFactory factory = new EventHandlerTypeFactory(WORKFLOW_TYPE);
		return factory;
	}

	@Test
	public void workflowStarted() throws Exception {
		final EventHandlerTypeFactory factory = createFactory();
		final Method method = Template1.class.getMethod("onWorkflowStarted");
		final EventHandlerType type = factory.create(method);
		assertThat(type)
				.isEqualTo(new EventHandlerType(EventType.WorkflowExecutionStarted, new WorkflowName(WORKFLOW_TYPE)));
	}

	@Test
	public void activityTaskCompleted() throws Exception {
		final EventHandlerTypeFactory factory = createFactory();
		final Method method = Template1.class.getMethod("onActivityCompleted");
		final EventHandlerType type = factory.create(method);
		assertThat(type)
				.isEqualTo(new EventHandlerType(EventType.ActivityTaskCompleted, new ActivityName(
						new VersionedName("activity1", "1"))));
	}

	@Test
	public void activityTaskFailed() throws Exception {
		final EventHandlerTypeFactory factory = createFactory();
		final Method method = Template1.class.getMethod("onActivityFailed");
		final EventHandlerType type = factory.create(method);
		assertThat(type)
				.isEqualTo(new EventHandlerType(EventType.ActivityTaskFailed, new ActivityName(
						new VersionedName("activity1", "1"))));
	}

	@Test
	public void signalReceived() throws Exception {
		final EventHandlerTypeFactory factory = createFactory();
		final Method method = Template1.class.getMethod("onSignalReceived");
		final EventHandlerType type = factory.create(method);
		assertThat(type)
				.isEqualTo(new EventHandlerType(EventType.WorkflowExecutionSignaled, new SignalName(SIGNAL1)));
	}

	@Test
	public void timerFired() throws Exception {
		final EventHandlerTypeFactory factory = createFactory();
		final Method method = Template1.class.getMethod("onTimerFired");
		final EventHandlerType type = factory.create(method);
		assertThat(type)
				.isEqualTo(new EventHandlerType(EventType.TimerFired, new TimerName(TIMER1)));
	}

    @Test
    public void markerRecorded() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onMarkerRecorded");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.MarkerRecorded, new MarkerName(MARKER1)));
    }

    @Test
    public void recordMarkerFailed() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onRecordMarkerFailed");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.RecordMarkerFailed, new MarkerName(MARKER1)));
    }

    /**
     * Test case: user can not specify timer with reserved ID.
     */
    @Test
    public void testTimerId() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onRetryTimerFired");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> factory.create(method))
                .withMessageContaining("This is reserved timer ID");
    }

    @Test
    public void onChildWorkflowCanceled() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onChildWorkflowCanceled");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.ChildWorkflowExecutionCanceled, CHILD_WORKFLOW_NAME));
    }

    @Test
    public void onChildWorkflowCompleted() throws Exception  {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onChildWorkflowCompleted");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.ChildWorkflowExecutionCompleted, CHILD_WORKFLOW_NAME));
    }

    @Test
    public void onChildWorkflowFailed() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onChildWorkflowFailed");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.ChildWorkflowExecutionFailed, CHILD_WORKFLOW_NAME));
    }

    @Test
    public void onChildWorkflowStarted() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onChildWorkflowStarted");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.ChildWorkflowExecutionStarted, CHILD_WORKFLOW_NAME));
    }

    @Test
    public void onChildWorkflowTerminated() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onChildWorkflowTerminated");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.ChildWorkflowExecutionTerminated, CHILD_WORKFLOW_NAME));
    }

    @Test
    public void onChildWorkflowTimedOut() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onChildWorkflowTimedOut");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.ChildWorkflowExecutionTimedOut, CHILD_WORKFLOW_NAME));
    }

    @Test
    public void onStartChildWorkflowFailed() throws Exception {
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onStartChildWorkflowFailed");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.StartChildWorkflowExecutionFailed, CHILD_WORKFLOW_NAME));
    }

    @Test
    public void onWorkflowCancelRequested() throws Exception{
        EventHandlerTypeFactory factory = createFactory();
        Method method = Template1.class.getMethod("onWorkflowCancelRequested");

        EventHandlerType type = factory.create(method);

        assertThat(type).isEqualTo(new EventHandlerType(EventType.WorkflowExecutionCancelRequested, new WorkflowName(WORKFLOW_TYPE)));
    }
}
