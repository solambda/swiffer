package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.amazonaws.services.simpleworkflow.model.MarkerRecordedEventAttributes;

/**
 * Annotated parameter is a details of last recorded Marker with specified name.
 * Accepts any type.
 *
 * @see MarkerRecordedEventAttributes#getDetails()
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Marker {

    /**
     * The Marker name.
     *
     * @return the name of the Marker
     */
    String value();
}
