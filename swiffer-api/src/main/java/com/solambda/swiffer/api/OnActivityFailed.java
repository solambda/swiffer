/**
 *
 */
package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.ActivityTaskFailedEventAttributes;

/**
 * Handler for the ActivityTaskFailed event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>any parameter that is common to all event handlers (see
 * {@link EventHandlerCommonParameter})
 * <li>the default parameter is {@link Long} {@code scheduledEventId}: The ID of the {@code ActivityTaskScheduled} event
 * that was recorded when this activity task was scheduled.
 * </ul>
 *
 * @see ActivityTaskFailedEventAttributes
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnActivityFailed {
    /**
     * The activity type that failed. Consider using {@link AllActivities} in
     * order to create an event handler for any type of activities.
     *
     * @return the activity type that failed
     */
    Class<?> activity();
}
