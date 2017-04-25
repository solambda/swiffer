package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.RequestCancelExternalWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.StartChildWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.solambda.swiffer.api.ActivityOptions;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.WorkflowOptions;
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.ActivityTaskFailedContext;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.RetryControl;
import com.solambda.swiffer.api.retry.RetryPolicy;

/**
 * Test for {@link DecisionsImpl}.
 */
public class DecisionsImplTest {
    private final DataMapper dataMapper = mock(DataMapper.class);
    private final DurationTransformer durationTransformer = mock(DurationTransformer.class);
    private final RetryPolicy globalRetryPolicy = mock(RetryPolicy.class);
    private final DecisionsImpl decisions = new DecisionsImpl(dataMapper, durationTransformer, globalRetryPolicy);

    private static final WorkflowType CHILD_WORKFLOW_TYPE = new WorkflowType().withName("child").withVersion("1");
    /**
     * Verify that marker details are serialized.
     */
    @Test
    public void recordMarker() {
        Object markerDetails = new Object();

        decisions.recordMarker("Marker", markerDetails);

        verify(dataMapper).serialize(markerDetails);
    }

    /**
     * Verify activity input is serialized.
     */
    @Test
    public void scheduleActivityTask() {
        Object input = new Object();

        decisions.scheduleActivityTask(CustomActivity.class, input);

        verify(dataMapper).serialize(input);
    }

    /**
     * Verify activity input is serialized.
     */
    @Test
    public void scheduleActivityTask_withOptions() {
        Object input = new Object();

        decisions.scheduleActivityTask(CustomActivity.class, input, mock(ActivityOptions.class));

        verify(dataMapper).serialize(input);
    }

    /**
     * Verify activity input is serialized.
     */
    @Test
    public void scheduleActivityTask_withActivityId() {
        Object input = new Object();

        decisions.scheduleActivityTask(CustomActivity.class, input, "activityId", mock(ActivityOptions.class));

        verify(dataMapper).serialize(input);
    }

    /**
     * Verify complete workflow result is serialized.
     */
    @Test
    public void completeWorkflow() {
        Object result = new Object();

        decisions.completeWorkflow(result);

        verify(dataMapper).serialize(result);
    }

    /**
     * Verify timer control is serialized; duration is passed through {@link DurationTransformer}
     */
    @Test
    public void startTimer() {
        Object control = new Object();
        Duration duration = Duration.ofDays(5);

        when(durationTransformer.transform(duration)).thenReturn(duration);

        decisions.startTimer("timerId", duration, control);

        verify(dataMapper).serialize(control);
        verify(durationTransformer).transform(duration);
    }

    @Test
    public void retryActivity() {
        Duration nextDuration = Duration.ofSeconds(10);
        String expectedTimerId = RetryControl.RETRY_TIMER + "activity";
        Decisions spy = spy(decisions);
        Long scheduledEventId = 777L;
        DecisionTaskContext context = mock(DecisionTaskContext.class);
        when(context.getMarkerDetails(anyString(), any())).thenReturn(Optional.of(5));
        when(durationTransformer.transform(nextDuration)).thenReturn(nextDuration);

        RetryPolicy retryPolicy = mock(RetryPolicy.class);
        when(retryPolicy.durationToNextTry(6)).thenReturn(Optional.of(nextDuration));

        spy.retryActivity(scheduledEventId, CustomActivity.class, context, retryPolicy);

        ArgumentCaptor<RetryControl> controlArgumentCaptor = ArgumentCaptor.forClass(RetryControl.class);
        verify(spy).startTimer(eq(expectedTimerId), eq(nextDuration), controlArgumentCaptor.capture());
        RetryControl retryTimerControl = controlArgumentCaptor.getValue();
        assertThat(retryTimerControl.getScheduledEventId()).isEqualTo(scheduledEventId);
    }

    @Test
    public void retryActivity_NoRetry() {
        Decisions spy = spy(decisions);
        Long scheduledEventId = 777L;
        DecisionTaskContext context = mock(DecisionTaskContext.class);
        when(context.getMarkerDetails(anyString(), any())).thenReturn(Optional.empty());

        RetryPolicy retryPolicy = mock(RetryPolicy.class);
        when(retryPolicy.durationToNextTry(1)).thenReturn(Optional.empty());

        spy.retryActivity(scheduledEventId, "act", context, retryPolicy);

        verify(spy, never()).startTimer(any(), any(), any());
    }

    @Test
    public void retryActivity_FromContext() {
        String activityName = "activity";
        String expectedTimerId = RetryControl.RETRY_TIMER + activityName;

        Duration nextDuration = Duration.ofSeconds(10);
        when(globalRetryPolicy.durationToNextTry(6)).thenReturn(Optional.of(nextDuration));

        VersionedName activityType = mock(VersionedName.class);
        when(activityType.name()).thenReturn(activityName);

        ActivityTaskFailedContext context = mock(ActivityTaskFailedContext.class);
        when(context.activityType()).thenReturn(activityType);
        when(context.getMarkerDetails(anyString(), any())).thenReturn(Optional.of(5));

        when(durationTransformer.transform(nextDuration)).thenReturn(nextDuration);

        Decisions spy = spy(decisions);
        Long scheduledEventId = 777L;
        spy.retryActivity(scheduledEventId, context);

        verify(globalRetryPolicy).durationToNextTry(6);

        ArgumentCaptor<RetryControl> controlArgumentCaptor = ArgumentCaptor.forClass(RetryControl.class);
        verify(spy).startTimer(eq(expectedTimerId), eq(nextDuration), controlArgumentCaptor.capture());
        RetryControl retryTimerControl = controlArgumentCaptor.getValue();
        assertThat(retryTimerControl.getScheduledEventId()).isEqualTo(scheduledEventId);
    }

    /**
     * Test case:
     * if request for cancel was received then activity should not be retried.
     */
    @Test
    public void retryActivity_AfterCancel() {
        Decisions spy = spy(decisions);
        Long scheduledEventId = 777L;
        DecisionTaskContext context = mock(DecisionTaskContext.class);
        when(context.isCancelRequested()).thenReturn(true);
        RetryPolicy retryPolicy = mock(RetryPolicy.class);

        spy.retryActivity(scheduledEventId, CustomActivity.class, context, retryPolicy);

        verify(spy, never()).startTimer(any(), any(), any());
        verifyZeroInteractions(durationTransformer);
    }

    @Test
    public void cancelWorkflow() throws Exception {
        String details = "Details";

        decisions.cancelWorkflow(details);

        assertThat(decisions.get()).hasSize(1);
        Optional<Decision> decision = decisions.get().stream().filter(d -> d.getDecisionType().equals(DecisionType.CancelWorkflowExecution.name())).findAny();
        assertThat(decision).isPresent();
        assertThat(decision.get().getCancelWorkflowExecutionDecisionAttributes().getDetails()).isEqualTo(details);
    }

    @Test
    public void startChildWorkflow() throws Exception {
        String workflowId = "workflowId";

        decisions.startChildWorkflow(ChildWorkflow.class, workflowId);

        assertThat(decisions.get()).hasSize(1);
        Optional<Decision> decision = decisions.get().stream().filter(d -> d.getDecisionType().equals(DecisionType.StartChildWorkflowExecution.name())).findAny();
        assertThat(decision).isPresent();
        StartChildWorkflowExecutionDecisionAttributes attributes = decision.get().getStartChildWorkflowExecutionDecisionAttributes();
        assertThat(attributes.getChildPolicy()).isNull();
        assertThat(attributes.getControl()).isNull();
        assertThat(attributes.getExecutionStartToCloseTimeout()).isNull();
        assertThat(attributes.getInput()).isNull();
        assertThat(attributes.getLambdaRole()).isNull();
        assertThat(attributes.getTagList()).isNull();
        assertThat(attributes.getTaskList()).isNull();
        assertThat(attributes.getTaskPriority()).isNull();
        assertThat(attributes.getTaskStartToCloseTimeout()).isNull();
        assertThat(attributes.getWorkflowId()).isEqualTo(workflowId);
        assertThat(attributes.getWorkflowType()).isEqualTo(CHILD_WORKFLOW_TYPE);
    }

    @Test
    public void startChildWorkflow1() throws Exception {
        String workflowId = "workflowId";
        String input = "input";

        when(dataMapper.serialize(input)).thenReturn(input);

        decisions.startChildWorkflow(ChildWorkflow.class, workflowId, input);

        verify(dataMapper).serialize(input);

        assertThat(decisions.get()).hasSize(1);
        Optional<Decision> decision = decisions.get().stream().filter(d -> d.getDecisionType().equals(DecisionType.StartChildWorkflowExecution.name())).findAny();
        assertThat(decision).isPresent();
        StartChildWorkflowExecutionDecisionAttributes attributes = decision.get().getStartChildWorkflowExecutionDecisionAttributes();
        assertThat(attributes.getChildPolicy()).isNull();
        assertThat(attributes.getControl()).isNull();
        assertThat(attributes.getExecutionStartToCloseTimeout()).isNull();
        assertThat(attributes.getInput()).isEqualTo(input);
        assertThat(attributes.getLambdaRole()).isNull();
        assertThat(attributes.getTagList()).isNull();
        assertThat(attributes.getTaskList()).isNull();
        assertThat(attributes.getTaskPriority()).isNull();
        assertThat(attributes.getTaskStartToCloseTimeout()).isNull();
        assertThat(attributes.getWorkflowId()).isEqualTo(workflowId);
        assertThat(attributes.getWorkflowType()).isEqualTo(CHILD_WORKFLOW_TYPE);
    }

    @Test
    public void startChildWorkflow2() throws Exception {
        String workflowId = "workflowId";
        Long input = 90L;
        String taskList = "custom-task-list";
        WorkflowOptions options = new WorkflowOptions().maxDecisionTaskDuration(Duration.ofHours(6))
                                                       .maxWorkflowDuration(Duration.ofDays(2))
                                                       .taskPriority(10)
                                                       .taskList(taskList)
                                                       .childTerminationPolicy(ChildPolicy.REQUEST_CANCEL);

        when(dataMapper.serialize(input)).thenReturn("90");

        decisions.startChildWorkflow(ChildWorkflow.class, workflowId, input, options);

        verify(dataMapper).serialize(input);

        assertThat(decisions.get()).hasSize(1);
        Optional<Decision> decision = decisions.get().stream().filter(d -> d.getDecisionType().equals(DecisionType.StartChildWorkflowExecution.name())).findAny();
        assertThat(decision).isPresent();

        StartChildWorkflowExecutionDecisionAttributes attributes = decision.get().getStartChildWorkflowExecutionDecisionAttributes();
        assertThat(attributes.getChildPolicy()).isEqualTo(ChildPolicy.REQUEST_CANCEL.toString());
        assertThat(attributes.getControl()).isNull();
        assertThat(attributes.getExecutionStartToCloseTimeout()).isEqualTo("172800");
        assertThat(attributes.getInput()).isEqualTo("90");
        assertThat(attributes.getLambdaRole()).isNull();
        assertThat(attributes.getTagList()).isNull();
        assertThat(attributes.getTaskList()).isEqualTo(new TaskList().withName(taskList));
        assertThat(attributes.getTaskPriority()).isEqualTo("10");
        assertThat(attributes.getTaskStartToCloseTimeout()).isEqualTo("21600");
        assertThat(attributes.getWorkflowId()).isEqualTo(workflowId);
        assertThat(attributes.getWorkflowType()).isEqualTo(CHILD_WORKFLOW_TYPE);
    }

    @Test(expected = NullPointerException.class)
    public void startChildWorkflow_NoType() throws Exception {
        decisions.startChildWorkflow(null, "workflowId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void startChildWorkflow_NoId() throws Exception {
        decisions.startChildWorkflow(ChildWorkflow.class, null);
    }

    @Test
    public void requestCancelExternalWorkflow() throws Exception {
        String workflowId = "workflow";
        String runId = "run";

        decisions.requestCancelExternalWorkflow(workflowId, runId);

        assertThat(decisions.get()).hasSize(1);
        Optional<Decision> decision = decisions.get().stream().filter(d -> d.getDecisionType().equals(DecisionType.RequestCancelExternalWorkflowExecution.name())).findAny();
        assertThat(decision).isPresent();
        RequestCancelExternalWorkflowExecutionDecisionAttributes attributes = decision.get().getRequestCancelExternalWorkflowExecutionDecisionAttributes();
        assertThat(attributes.getWorkflowId()).isEqualTo(workflowId);
        assertThat(attributes.getControl()).isNull();
        assertThat(attributes.getRunId()).isEqualTo(runId);
    }

    @Test
    public void requestCancelExternalWorkflow1() throws Exception {
        String workflowId = "workflow";
        String runId = "run";
        Long control = 888L;

        when(dataMapper.serialize(control)).thenReturn("888");

        decisions.requestCancelExternalWorkflow(workflowId, runId, control);

        verify(dataMapper).serialize(control);

        assertThat(decisions.get()).hasSize(1);
        Optional<Decision> decision = decisions.get().stream().filter(d -> d.getDecisionType().equals(DecisionType.RequestCancelExternalWorkflowExecution.name())).findAny();
        assertThat(decision).isPresent();
        RequestCancelExternalWorkflowExecutionDecisionAttributes attributes = decision.get().getRequestCancelExternalWorkflowExecutionDecisionAttributes();
        assertThat(attributes.getWorkflowId()).isEqualTo(workflowId);
        assertThat(attributes.getControl()).isEqualTo("888");
        assertThat(attributes.getRunId()).isEqualTo(runId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void requestCancelExternalWorkflow_NoWorkflowId() throws Exception {
        decisions.requestCancelExternalWorkflow(null, "run");
    }

    @Test(expected = NullPointerException.class)
    public void requestCancelExternalWorkflow_NoRunId() throws Exception {
        decisions.requestCancelExternalWorkflow("workflow", null);
    }

    @ActivityType(name = "activity", version="1")
    @interface CustomActivity{}

    @com.solambda.swiffer.api.WorkflowType(name = "child", version = "1")
    public @interface ChildWorkflow {

    }
}