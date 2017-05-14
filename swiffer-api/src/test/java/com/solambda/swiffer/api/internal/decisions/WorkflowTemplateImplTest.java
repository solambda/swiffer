package com.solambda.swiffer.api.internal.decisions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.simpleworkflow.model.CancelWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.ContinueAsNewWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.EventType;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionFailedCause;
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.events.EventCategory;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class WorkflowTemplateImplTest {

    private final DataMapper dataMapper = mock(DataMapper.class);
    private final DurationTransformer durationTransformer = mock(DurationTransformer.class);
    private final RetryPolicy globalRetryPolicy = mock(RetryPolicy.class);
    private final EventHandlerRegistry eventHandlerRegistry = mock(EventHandlerRegistry.class);
    private final VersionedName workflowType = mock(VersionedName.class);

    private final WorkflowTemplateImpl template = new WorkflowTemplateImpl(workflowType, eventHandlerRegistry, dataMapper, durationTransformer, globalRetryPolicy);

    private final DecisionTaskContext context = mock(DecisionTaskContext.class);

    @Test
    public void decide_CompleteWorkflowFailed() throws Exception {
        String cause = CompleteWorkflowExecutionFailedCause.UNHANDLED_DECISION.name();
        WorkflowEvent event = mock(WorkflowEvent.class);
        when(event.category()).thenReturn(EventCategory.WORKFLOW_EXECUTION);
        when(event.type()).thenReturn(EventType.CompleteWorkflowExecutionFailed);
        when(event.cause()).thenReturn(cause);

        List<WorkflowEvent> newEvents = Arrays.asList(event);
        EventHandler defaultEventHandler = mock(EventHandler.class);

        when(eventHandlerRegistry.getDefaultCompleteWorkflowExecutionFailedHandler(any())).thenReturn(defaultEventHandler);
        when(context.newEvents()).thenReturn(newEvents);

        template.decide(context);

        verify(eventHandlerRegistry).getDefaultCompleteWorkflowExecutionFailedHandler(cause);
        verify(defaultEventHandler).handleEvent(any(), any());
    }

    @Test
    public void decide_CancelWorkflowFailed() throws Exception {
        String cause = CancelWorkflowExecutionFailedCause.UNHANDLED_DECISION.name();
        WorkflowEvent event = mock(WorkflowEvent.class);
        when(event.category()).thenReturn(EventCategory.WORKFLOW_EXECUTION);
        when(event.type()).thenReturn(EventType.CancelWorkflowExecutionFailed);
        when(event.cause()).thenReturn(cause);

        List<WorkflowEvent> newEvents = Arrays.asList(event);
        EventHandler defaultEventHandler = mock(EventHandler.class);

        when(eventHandlerRegistry.getDefaultCancelWorkflowExecutionFailedHandler(any())).thenReturn(defaultEventHandler);
        when(context.newEvents()).thenReturn(newEvents);

        template.decide(context);

        verify(eventHandlerRegistry).getDefaultCancelWorkflowExecutionFailedHandler(cause);
        verify(defaultEventHandler).handleEvent(any(), any());
    }

    @Test
    public void decide_FailWorkflowFailed() throws Exception {
        String cause = FailWorkflowExecutionFailedCause.UNHANDLED_DECISION.name();
        WorkflowEvent event = mock(WorkflowEvent.class);
        when(event.category()).thenReturn(EventCategory.WORKFLOW_EXECUTION);
        when(event.type()).thenReturn(EventType.FailWorkflowExecutionFailed);
        when(event.cause()).thenReturn(cause);

        List<WorkflowEvent> newEvents = Arrays.asList(event);
        EventHandler defaultEventHandler = mock(EventHandler.class);

        when(eventHandlerRegistry.getDefaultFailWorkflowExecutionFailedHandler(any())).thenReturn(defaultEventHandler);
        when(context.newEvents()).thenReturn(newEvents);

        template.decide(context);

        verify(eventHandlerRegistry).getDefaultFailWorkflowExecutionFailedHandler(cause);
        verify(defaultEventHandler).handleEvent(any(), any());
    }

    @Test
    public void decide_ContinueAsNewWorkflowFailed() throws Exception {
        String cause = ContinueAsNewWorkflowExecutionFailedCause.UNHANDLED_DECISION.name();
        WorkflowEvent event = mock(WorkflowEvent.class);
        when(event.category()).thenReturn(EventCategory.WORKFLOW_EXECUTION);
        when(event.type()).thenReturn(EventType.ContinueAsNewWorkflowExecutionFailed);
        when(event.cause()).thenReturn(cause);

        List<WorkflowEvent> newEvents = Arrays.asList(event);
        EventHandler defaultEventHandler = mock(EventHandler.class);

        when(eventHandlerRegistry.getDefaultContinueAsNewWorkflowExecutionFailedHandler(any())).thenReturn(defaultEventHandler);
        when(context.newEvents()).thenReturn(newEvents);

        template.decide(context);

        verify(eventHandlerRegistry).getDefaultContinueAsNewWorkflowExecutionFailedHandler(cause);
        verify(defaultEventHandler).handleEvent(any(), any());
    }
}