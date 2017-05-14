package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.Test;

import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.context.ActivityTaskFailedContext;
import com.solambda.swiffer.api.internal.context.ActivityTaskTimedOutContext;
import com.solambda.swiffer.api.internal.handler.CloseWorkflowControl;
import com.solambda.swiffer.api.internal.handler.CloseWorkflowFailedHandlers;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.NoRetryPolicy;
import com.solambda.swiffer.api.retry.RetryControl;
import com.solambda.swiffer.api.retry.RetryHandlers;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class EventHandlerFactoryTest {

    private VersionedName workflowType = mock(VersionedName.class);
    private DataMapper dataMapper = mock(DataMapper.class);
    private RetryPolicy retryPolicy = mock(RetryPolicy.class);

    private final Method onFailureMethod = getOnFailureMethod();
    private final Method onTimeoutMethod = getOnTimeoutMethod();
    private final Method onRetryTimerFiredMethod = getOnRetryTimerFiredMethod();
    private final RetryPolicy noRetryPolicy = new NoRetryPolicy();

    @Test
    public void createFailedActivityHandler() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, retryPolicy));
        EventHandler handler = eventHandlerFactory.createFailedActivityHandler();

        verify(eventHandlerFactory).createEventHandler(any(RetryHandlers.class), eq(EventHandlerFactory.FAILED_ACTIVITY), eq(onFailureMethod));
        assertThat(handler).isNotNull();
    }

    @Test
    public void createTimedOutActivityHandler() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, retryPolicy));
        EventHandler handler = eventHandlerFactory.createTimedOutActivityHandler();

        verify(eventHandlerFactory).createEventHandler(any(RetryHandlers.class), eq(EventHandlerFactory.TIMED_OUT_ACTIVITY), eq(onTimeoutMethod));
        assertThat(handler).isNotNull();
    }

    @Test
    public void createRetryTimerFiredHandler() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, retryPolicy));
        EventHandler handler = eventHandlerFactory.createRetryTimerFiredHandler();

        verify(eventHandlerFactory).createEventHandler(any(RetryHandlers.class), eq(EventHandlerFactory.RETRY_TIMER_FIRED), eq(onRetryTimerFiredMethod));
        assertThat(handler).isNotNull();
    }

    @Test
    public void createFailedActivityHandler_NoRetryPolicy() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, noRetryPolicy));
        EventHandler handler = eventHandlerFactory.createFailedActivityHandler();

        verify(eventHandlerFactory, never()).createEventHandler(any(), any(), any());
        assertThat(handler).isNull();
    }

    @Test
    public void createTimedOutActivityHandler_NoRetryPolicy() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, noRetryPolicy));
        EventHandler handler = eventHandlerFactory.createTimedOutActivityHandler();

        verify(eventHandlerFactory, never()).createEventHandler(any(), any(), any());
        assertThat(handler).isNull();
    }

    @Test
    public void createRetryTimerFiredHandler_NoRetryPolicy() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, noRetryPolicy));
        EventHandler handler = eventHandlerFactory.createRetryTimerFiredHandler();

        verify(eventHandlerFactory).createEventHandler(any(RetryHandlers.class), eq(EventHandlerFactory.RETRY_TIMER_FIRED), eq(onRetryTimerFiredMethod));
        assertThat(handler).isNotNull();
    }

    @Test
    public void createCompleteWorkflowExecutionFailedHandler() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, noRetryPolicy));
        EventHandler handler = eventHandlerFactory.createCompleteWorkflowExecutionFailedHandler();

        Method expectedMethod = getCloseWorkflowHandler("onCompleteWorkflowExecutionFailed");
        verify(eventHandlerFactory).createEventHandler(any(CloseWorkflowFailedHandlers.class),
                                                       eq(EventHandlerFactory.COMPLETE_WORKFLOW_EXECUTION_FAILED),
                                                       eq(expectedMethod));
        assertThat(handler).isNotNull();
    }

    @Test
    public void createCancelWorkflowExecutionFailedHandler() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, noRetryPolicy));
        EventHandler handler = eventHandlerFactory.createCancelWorkflowExecutionFailedHandler();

        Method expectedMethod = getCloseWorkflowHandler("onCancelWorkflowExecutionFailed");
        verify(eventHandlerFactory).createEventHandler(any(CloseWorkflowFailedHandlers.class),
                                                       eq(EventHandlerFactory.CANCEL_WORKFLOW_EXECUTION_FAILED),
                                                       eq(expectedMethod));
        assertThat(handler).isNotNull();
    }

    @Test
    public void createFailWorkflowExecutionFailedHandler() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, noRetryPolicy));
        EventHandler handler = eventHandlerFactory.createFailWorkflowExecutionFailedHandler();

        Method expectedMethod = getCloseWorkflowHandler("onFailWorkflowExecutionFailed");
        verify(eventHandlerFactory).createEventHandler(any(CloseWorkflowFailedHandlers.class),
                                                       eq(EventHandlerFactory.FAIL_WORKFLOW_EXECUTION_FAILED),
                                                       eq(expectedMethod));
        assertThat(handler).isNotNull();
    }

    @Test
    @Ignore
    public void createContinueAsNewWorkflowExecutionFailedHandler() throws Exception {
        EventHandlerFactory eventHandlerFactory = spy(new EventHandlerFactory(workflowType, dataMapper, noRetryPolicy));
        EventHandler handler = eventHandlerFactory.createContinueAsNewWorkflowExecutionFailedHandler();
        //TODO: add after issue #11 is fixed
        fail("");
    }

    private static Method getOnFailureMethod() {
        try {
            return RetryHandlers.class.getMethod("onFailure", Long.class, Decisions.class, ActivityTaskFailedContext.class);
        } catch (NoSuchMethodException e) {
            fail("Can't create onFailure Method", e);
            return null;
        }
    }

    private static Method getOnTimeoutMethod() {
        try {
            return RetryHandlers.class.getMethod("onTimeout", Long.class, Decisions.class, ActivityTaskTimedOutContext.class);
        } catch (NoSuchMethodException e) {
            fail("Can't create onTimeout Method", e);
            return null;
        }
    }

    private static Method getOnRetryTimerFiredMethod() {
        try {
            return RetryHandlers.class.getMethod("onTimer", RetryControl.class, Decisions.class, DecisionTaskContext.class);
        } catch (NoSuchMethodException e) {
            fail("Can't create onTimer Method", e);
            return null;
        }
    }

    private Method getCloseWorkflowHandler(String methodName) {
        try {
            return CloseWorkflowFailedHandlers.class.getMethod(methodName, CloseWorkflowControl.class, Decisions.class);
        } catch (NoSuchMethodException e) {
            fail("Can't get method", e);
            return null;
        }
    }
}