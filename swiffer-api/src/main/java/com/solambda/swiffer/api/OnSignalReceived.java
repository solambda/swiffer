/**
 *
 */
package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Handler for the WorkflowExecutionSignaled event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers (see
 * {@link EventHandlerCommonParameter})
 * <li>the default parameter is <code>input</code>: Inputs provided with the
 * signal (if any). The decider can use the signal name and inputs to determine
 * how to process the signal.
 * </ul>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnSignalReceived {
	/**
	 * The name of the signal received.
	 *
	 * @return the id of the timer that fired.
	 */
	String signalName();
}
