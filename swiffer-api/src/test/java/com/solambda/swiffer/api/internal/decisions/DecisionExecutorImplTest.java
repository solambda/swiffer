package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ArgumentCaptor<RespondDecisionTaskCompletedRequest> requestCaptor = ArgumentCaptor.forClass(RespondDecisionTaskCompletedRequest.class);

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
        List<Decision> decisionList = mockAllNotCloseDecisions();
        when(decisions.get()).thenReturn(decisionList);

        decisionExecutor.apply(context, decisions);

        verify(swf).respondDecisionTaskCompleted(requestCaptor.capture());
        RespondDecisionTaskCompletedRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.getDecisions()).containsExactlyElementsOf(decisionList);
    }

    private Object[] closeDecisions() {
        return new Object[]{
                mockDecision(DecisionType.CancelWorkflowExecution),
                mockDecision(DecisionType.CompleteWorkflowExecution),
                mockDecision(DecisionType.ContinueAsNewWorkflowExecution),
                mockDecision(DecisionType.FailWorkflowExecution)
        };
    }

    /**
     * Test case: only one close decision should be present.
     * All decisions which comes after the first one are filtered out.
     * All decisions which are incompatible with close and comes before are also removed.
     */
    @Test
    @Parameters(method = "closeDecisions")
    public void apply_DecisionsBeforeAndAfterClose(Decision closeDecision) throws Exception {
        Collection<Decision> closeDecisions = mockAllCloseDecisions();
        List<Decision> allNotClose = mockAllNotCloseDecisions();
        List<Decision> decisionList = new ArrayList<>(allNotClose);
        decisionList.add(closeDecision); // this is the close decision that should remain
        decisionList.addAll(closeDecisions);
        decisionList.addAll(mockAllNotCloseDecisions());  // another non-close decisions after close, they also should be removed

        when(decisions.get()).thenReturn(decisionList);

        decisionExecutor.apply(context, decisions);

        verify(swf).respondDecisionTaskCompleted(requestCaptor.capture());

        List<Decision> expected = allNotClose.stream().filter(d -> compatibleWithClose.contains(d.getDecisionType())).collect(Collectors.toList());
        expected.add(closeDecision);

        RespondDecisionTaskCompletedRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.getDecisions()).containsExactlyElementsOf(expected);
    }

    /**
     * Test case: decisions that are not compatible with close are removed.
     * All decision scheduled before close and which are compatible are present.
     */
    @Test
    @Parameters(method = "closeDecisions")
    public void apply_HasCloseDecision(Decision closeDecision) throws Exception {
        List<Decision> decisionList = new ArrayList<>(mockAllNotCloseDecisions());
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

    private List<Decision> mockAllNotCloseDecisions() {
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
}