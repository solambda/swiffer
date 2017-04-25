package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.ChildWorkflowExecutionStartedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.EventType;

/**
 * Handler for the {@link EventType#ChildWorkflowExecutionStarted} event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers
 * <li>the default parameter is {@link String} {@code runId}: A system-generated unique identifier for the child workflow execution.
 * </ul>
 *
 * @see EventHandlerCommonParameter
 * @see ChildWorkflowExecutionStartedEventAttributes
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnChildWorkflowStarted {

    /**
     * The child {@link WorkflowType} that was started.
     *
     * @return the started child workflow type
     */
    Class<?> value();
}
