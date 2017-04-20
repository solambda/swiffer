package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.ChildWorkflowExecutionTimedOutEventAttributes;
import com.amazonaws.services.simpleworkflow.model.EventType;

/**
 * Handler for the {@link EventType#ChildWorkflowExecutionTimedOut} event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers
 * <li>the default parameter is {@code initiatedEventId}: the ID of the {@code StartChildWorkflowExecutionInitiated} event
 * corresponding to the {@code StartChildWorkflowExecution} decision to start this child workflow execution.
 * </ul>
 *
 * @see EventHandlerCommonParameter
 * @see ChildWorkflowExecutionTimedOutEventAttributes
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnChildWorkflowTimedOut {

    /**
     * The child {@link WorkflowType} that timed out.
     *
     * @return the timed out workflow type
     */
    Class<?> value();

}
