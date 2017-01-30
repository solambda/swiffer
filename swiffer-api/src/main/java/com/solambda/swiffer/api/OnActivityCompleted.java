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
 * Handler for the ActivityTaskCompleted event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers (see
 * {@link EventHandlerCommonParameter})
 * <li>the default parameter is <code>result</code>: the result output of the
 * activity (if any).
 * <li><code>@{@link Input} MyInputObject input</code>: the input of the
 * activity that is completed
 * </ul>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnActivityCompleted {
	/**
	 * The activity type that is completed. Consider using {@link AllActivities}
	 * in order to create an event handler for any type of activities.
	 *
	 * @return the activity type that is completed
	 */
	Class<?> value();

}
