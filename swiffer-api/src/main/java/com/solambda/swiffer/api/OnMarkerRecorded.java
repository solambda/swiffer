package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Handler for the {@code MarkerRecorded} event.
 * <p>
 * The annotated method may have the following parameters:
 * <ul>
 * <li>
 * any parameter that is common to all event handlers (see {@link EventHandlerCommonParameter})
 * </li>
 * <li>
 * the default parameter is {@code input}: the optional details recorded with Marker.
 * </li>
 * </ul>
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
@EventHandler
public @interface OnMarkerRecorded {

    /**
     * The name of the recoded Marker.
     *
     * @return name of the recoded Marker
     */
    String value();
}
