package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.RespondDecisionTaskCompletedRequest;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;


@RunWith(JUnitParamsRunner.class)
public class DecisionExecutorImplTest {

    private final AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
    private final DecisionExecutorImpl decisionExecutor = new DecisionExecutorImpl(swf);

    private final DecisionTaskContext context = mock(DecisionTaskContext.class);
    private final DecisionsImpl decisions = mock(DecisionsImpl.class);

    @Captor
    private ArgumentCaptor<RespondDecisionTaskCompletedRequest> requestCaptor = ArgumentCaptor.forClass(RespondDecisionTaskCompletedRequest.class);

    private final List<String> compatibleWithClose = Arrays.asList(DecisionType.CancelTimer.name(),
                                                                   DecisionType.RecordMarker.name(),
                                                                   DecisionType.StartChildWorkflowExecution.name(),
                                                                   DecisionType.RequestCancelExternalWorkflowExecution.name());

    /**
     * Test case: there is no close decision in the list.
     * Expected result: all decisions should be passed to server (i.e. nothing is removed)
     */
    @Test
    public void apply() throws Exception {
        Collection<Decision> decisionList = mockAllNotCloseDecisions();
        when(decisions.get()).thenReturn(decisionList);

        decisionExecutor.apply(context, decisions);

        verify(swf).respondDecisionTaskCompleted(requestCaptor.capture());
        RespondDecisionTaskCompletedRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.getDecisions()).containsOnlyElementsOf(decisionList);
    }

    /**
     * Test case: close decisions are compatible with one another and shouldn't be filtered out.
     */
    @Test
    public void apply_HasSeveralCloseDecision() throws Exception {
        Collection<Decision> closeDecisions = mockAllCloseDecisions();
        Collection<Decision> decisionList = new ArrayList<>(mockAllNotCloseDecisions());
        decisionList.addAll(closeDecisions);

        when(decisions.get()).thenReturn(decisionList);

        decisionExecutor.apply(context, decisions);

        verify(swf).respondDecisionTaskCompleted(requestCaptor.capture());
        RespondDecisionTaskCompletedRequest actualRequest = requestCaptor.getValue();
        Collection<String> expectedDecisions = new ArrayList<>(compatibleWithClose);
        expectedDecisions.add(DecisionType.CancelWorkflowExecution.name());
        expectedDecisions.add(DecisionType.CompleteWorkflowExecution.name());
        expectedDecisions.add(DecisionType.FailWorkflowExecution.name());
        expectedDecisions.add(DecisionType.ContinueAsNewWorkflowExecution.name());

        assertThat(actualRequest.getDecisions()).extracting("decisionType").containsOnlyElementsOf(expectedDecisions);
    }

    private Object[] closeDecisions() {
        return new Object[]{
                mockDecision(DecisionType.CancelWorkflowExecution),
                mockDecision(DecisionType.CompleteWorkflowExecution),
                mockDecision(DecisionType.ContinueAsNewWorkflowExecution),
                mockDecision(DecisionType.FailWorkflowExecution)
        };
    }

    @Test
    @Parameters(method = "closeDecisions")
    public void apply_HasCloseDecision(Decision closeDecision) throws Exception {
        Collection<Decision> decisionList = new ArrayList<>(mockAllNotCloseDecisions());
        decisionList.add(closeDecision);

        when(decisions.get()).thenReturn(decisionList);

        decisionExecutor.apply(context, decisions);

        verify(swf).respondDecisionTaskCompleted(requestCaptor.capture());
        RespondDecisionTaskCompletedRequest actualRequest = requestCaptor.getValue();
        Collection<String> expectedDecisions = new ArrayList<>(compatibleWithClose);
        expectedDecisions.add(closeDecision.getDecisionType());

        assertThat(actualRequest.getDecisions()).extracting("decisionType").containsOnlyElementsOf(expectedDecisions);
    }

    private Decision mockDecision(DecisionType type){
        Decision decision = mock(Decision.class);
        when(decision.getDecisionType()).thenReturn(type.name());

        return decision;
    }

    private Collection<Decision> mockAllNotCloseDecisions() {
        return Arrays.asList(mockDecision(DecisionType.ScheduleActivityTask),
                             mockDecision(DecisionType.RequestCancelActivityTask),
                             mockDecision(DecisionType.RecordMarker),
                             mockDecision(DecisionType.StartTimer),
                             mockDecision(DecisionType.CancelTimer),
                             mockDecision(DecisionType.RequestCancelExternalWorkflowExecution),
                             mockDecision(DecisionType.StartChildWorkflowExecution));
    }

    private Collection<Decision> mockAllCloseDecisions() {
        return Arrays.asList(mockDecision(DecisionType.CancelWorkflowExecution),
                             mockDecision(DecisionType.CompleteWorkflowExecution),
                             mockDecision(DecisionType.ContinueAsNewWorkflowExecution),
                             mockDecision(DecisionType.FailWorkflowExecution));
    }

//    @Test
//    public void scheduleActivityTask_AfterCancel() throws Exception {
//        Duration duration = Duration.ofHours(8);
//        when(durationTransformer.transform(duration)).thenReturn(duration);
//
//        decisions.startTimer("Timer", duration)
//                 .cancelWorkflow("Cancel")
//                 .scheduleActivityTask(DecisionsImplTest.CustomActivity.class, "param");
//
//        assertThat(decisions.get()).hasSize(2);
//        assertThat(decisions.get()).extracting("decisionType").containsExactly(DecisionType.StartTimer.name(),
//                                                                               DecisionType.CancelWorkflowExecution.name());
//    }
//
//    @Test
//    public void startTimer_AfterComplete() throws Exception {
//        Duration duration = Duration.ofHours(8);
//        when(durationTransformer.transform(duration)).thenReturn(duration);
//
//        decisions.scheduleActivityTask(DecisionsImplTest.CustomActivity.class, "param")
//                 .completeWorkflow()
//                 .startTimer("Timer", duration);
//
//        assertThat(decisions.get()).hasSize(2);
//        assertThat(decisions.get()).extracting("decisionType").containsExactly(DecisionType.ScheduleActivityTask.name(),
//                                                                               DecisionType.CompleteWorkflowExecution.name());
//    }
//
//    @Test
//    public void recordMarker_AfterFail() throws Exception {
//        decisions.scheduleActivityTask(DecisionsImplTest.CustomActivity.class, "param")
//                 .failWorkflow("Timer", "Failure")
//                 .recordMarker("marker");
//
//        assertThat(decisions.get()).hasSize(2);
//        assertThat(decisions.get()).extracting("decisionType").containsExactly(DecisionType.ScheduleActivityTask.name(),
//                                                                               DecisionType.FailWorkflowExecution.name());
//
//    }
//
//    @Test
//    public void noFinalDecisions() throws Exception {
//        Duration duration = Duration.ofHours(8);
//        when(durationTransformer.transform(duration)).thenReturn(duration);
//
//        decisions.scheduleActivityTask(DecisionsImplTest.CustomActivity.class, "param")
//                 .recordMarker("marker")
//                 .startTimer("Timer", duration);
//
//        assertThat(decisions.get()).hasSize(3);
//        assertThat(decisions.get()).extracting("decisionType").containsExactly(DecisionType.ScheduleActivityTask.name(),
//                                                                               DecisionType.RecordMarker.name(),
//                                                                               DecisionType.StartTimer.name());
//    }
}