package com.solambda.swiffer.api.internal.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.solambda.swiffer.api.Decisions;

public class CloseWorkflowFailedHandlersTest {

    private final CloseWorkflowFailedHandlers handlers = new CloseWorkflowFailedHandlers();

    private Decisions decideTo = mock(Decisions.class);
    private CloseWorkflowControl control = mock(CloseWorkflowControl.class);

    @Test
    public void onCompleteWorkflowExecutionFailed() throws Exception {
        Object result = mock(Object.class);
        when(control.getResult()).thenReturn(result);

        handlers.onCompleteWorkflowExecutionFailed(control, decideTo);

        verify(decideTo).completeWorkflow(result);
    }

    @Test
    public void onCompleteWorkflowExecutionFailed_noControl() throws Exception {
        handlers.onCompleteWorkflowExecutionFailed(null, decideTo);

        verify(decideTo).completeWorkflow();
    }

    @Test
    public void onFailWorkflowExecutionFailed() throws Exception {
        String reason = "Any String";
        String details = "Any Details";
        when(control.getReason()).thenReturn(reason);
        when(control.getDetails()).thenReturn(details);

        handlers.onFailWorkflowExecutionFailed(control, decideTo);

        verify(decideTo).failWorkflow(reason, details);
    }

    @Test
    public void onFailWorkflowExecutionFailed_NoControl() throws Exception {
        handlers.onFailWorkflowExecutionFailed(null, decideTo);

        verify(decideTo).failWorkflow(null, null);
    }

    @Test
    public void onCancelWorkflowExecutionFailed() throws Exception {
        String details = "Any Details";
        when(control.getDetails()).thenReturn(details);

        handlers.onCancelWorkflowExecutionFailed(control, decideTo);

        verify(decideTo).cancelWorkflow(details);
    }

    @Test
    public void onCancelWorkflowExecutionFailed_NoControl() throws Exception {
        handlers.onCancelWorkflowExecutionFailed(null, decideTo);

        verify(decideTo).cancelWorkflow(null);
    }
}