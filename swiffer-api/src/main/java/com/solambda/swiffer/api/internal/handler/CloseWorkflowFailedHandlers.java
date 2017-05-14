package com.solambda.swiffer.api.internal.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.model.CancelWorkflowExecutionFailedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionFailedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.ContinueAsNewWorkflowExecutionFailedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.EventType;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionFailedEventAttributes;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Marker;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskContext;

/**
 * Provides handlers for close workflow failed events.
 * Handlers attempt to execute failed decision again with the same attributes.
 * <p>
 * Handles events:
 * <ul>
 * <li>{@link EventType#CompleteWorkflowExecutionFailed}</li>
 * <li>{@link EventType#FailWorkflowExecutionFailed}</li>
 * <li>{@link EventType#CancelWorkflowExecutionFailed}</li>
 * <li>{@link EventType#ContinueAsNewWorkflowExecutionFailed}</li>
 * </ul>
 */
public final class CloseWorkflowFailedHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseWorkflowFailedHandlers.class);

    /**
     * Default handler for {@link EventType#CompleteWorkflowExecutionFailed} event.
     *
     * @param control  the {@link CloseWorkflowControl} containing information for {@link DecisionType#CompleteWorkflowExecution} decision that has failed
     * @param decideTo the {@link Decisions} object
     * @see CompleteWorkflowExecutionFailedEventAttributes
     */
    public void onCompleteWorkflowExecutionFailed(@Marker(CloseWorkflowControl.COMPLETE_MARKER) CloseWorkflowControl control, Decisions decideTo) {
        LOGGER.debug("Reschedule <CompleteWorkflowExecution> decision after failure. Initial attributes: {}.", control);
        if (control != null) {
            decideTo.completeWorkflow(control.getResult());
        } else {
            LOGGER.warn("Control for previous decision not found, reschedule <CompleteWorkflowExecution> without attributes.");
            decideTo.completeWorkflow();
        }
    }

    /**
     * Default handler for {@link EventType#FailWorkflowExecutionFailed} event.
     *
     * @param control  the {@link CloseWorkflowControl} containing information for {@link DecisionType#FailWorkflowExecution} decision that has failed
     * @param decideTo the {@link Decisions} object
     * @see FailWorkflowExecutionFailedEventAttributes
     */
    public void onFailWorkflowExecutionFailed(@Marker(CloseWorkflowControl.FAIL_MARKER) CloseWorkflowControl control, Decisions decideTo) {
        LOGGER.debug("Reschedule <FailWorkflowExecution> decision after failure. Initial attributes: {}.", control);
        if (control != null) {
            decideTo.failWorkflow(control.getReason(), control.getDetails());
        } else {
            LOGGER.warn("Control for previous decision not found, reschedule <FailWorkflowExecution> without attributes.");
            decideTo.failWorkflow(null, null);
        }
    }

    /**
     * Default handler for {@link EventType#CancelWorkflowExecutionFailed} event.
     *
     * @param control  the {@link CloseWorkflowControl} containing information for {@link DecisionType#CancelWorkflowExecution} decision that has failed
     * @param decideTo the {@link Decisions} object
     * @see CancelWorkflowExecutionFailedEventAttributes
     */
    public void onCancelWorkflowExecutionFailed(@Marker(CloseWorkflowControl.CANCEL_MARKER) CloseWorkflowControl control, Decisions decideTo) {
        LOGGER.debug("Reschedule <CancelWorkflowExecution> decision after failure. Initial attributes: {}.", control);
        if (control != null) {
            decideTo.cancelWorkflow(control.getDetails());
        } else {
            LOGGER.warn("Control for previous decision not found, reschedule <CancelWorkflowExecution> without attributes.");
            decideTo.cancelWorkflow(null);
        }
    }

    /**
     * Default handler for {@link EventType#ContinueAsNewWorkflowExecutionFailed} event.
     *
     * @param decideTo the {@link Decisions} object
     * @param context  the failed task context
     * @see ContinueAsNewWorkflowExecutionFailedEventAttributes
     */
    public void onContinueAsNewWorkflowExecutionFailed(Decisions decideTo, DecisionTaskContext context) {
        // TODO: revisit this implementation in Issue #11
        LOGGER.debug("Reschedule <ContinueAsNewWorkflowExecution> decision after failure. Initial attributes: {}.");
        decideTo.continueAsNewWorkflow("1");
    }

}
