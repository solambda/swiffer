package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.CancelWorkflowExecutionFailedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.EventType;

/**
 * Handler for the {@link EventType#ChildWorkflowExecutionFailed} event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers
 * <li>the default parameter is {@code reason}: provided failure reason (if any).
 * </ul>
 *
 * @see EventHandlerCommonParameter
 * @see CancelWorkflowExecutionFailedEventAttributes
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnChildWorkflowFailed {

    /**
     * The child {@link WorkflowType} that failed.
     *
     * @return the failed workflow type
     */
    Class<?> value();
}
