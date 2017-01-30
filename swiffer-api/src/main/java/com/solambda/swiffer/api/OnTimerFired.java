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
 * Handler for the TimerFired event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers (see
 * {@link EventHandlerCommonParameter})
 * <li>the default parameter is <code>control</code>: Optional. Data attached to
 * the event that can be used by the decider in subsequent workflow tasks.
 * </ul>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnTimerFired {
	/**
	 * The id of the timer that fired.
	 *
	 * @return the id of the timer that fired.
	 */
	String value();

}
