package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionCancelRequestedEventAttributes;

/**
 * Handler for the {@link EventType#WorkflowExecutionCancelRequested} event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers
 * <li>the default parameter is {@code cause}: cause for requested workflow cancellation (optional)
 * </ul>
 *
 * @see EventHandlerCommonParameter
 * @see WorkflowExecutionCancelRequestedEventAttributes
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnWorkflowCancelRequested {
}
