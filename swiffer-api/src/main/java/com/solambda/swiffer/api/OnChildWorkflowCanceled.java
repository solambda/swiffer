package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.ChildWorkflowExecutionCanceledEventAttributes;
import com.amazonaws.services.simpleworkflow.model.EventType;

/**
 * Handler for the {@link EventType#ChildWorkflowExecutionCanceled} event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers
 * <li>the default parameter is {@link String} {@code details}: the details provided for workflow cancellation (if any).
 * </ul>
 *
 * @see EventHandlerCommonParameter
 * @see ChildWorkflowExecutionCanceledEventAttributes
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnChildWorkflowCanceled {

    /**
     * The child {@link WorkflowType} that was cancelled.
     *
     * @return the cancelled workflow type
     */
    Class<?> value();
}
