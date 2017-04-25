package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.ChildWorkflowExecutionCompletedEventAttributes;
import com.amazonaws.services.simpleworkflow.model.EventType;

/**
 * Handler for the {@link EventType#ChildWorkflowExecutionCompleted} event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers
 * <li>the default parameter is {@link Output} {@code result}: the result of the completed workflow (if any)
 * </ul>
 *
 * @see EventHandlerCommonParameter
 * @see ChildWorkflowExecutionCompletedEventAttributes
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnChildWorkflowCompleted {

    /**
     * The child {@link WorkflowType} that was completed.
     *
     * @return the completed workflow type
     */
    Class<?> value();
}
