package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A documentation purpose annotation that qualify another annotation as an
 * event handler.
 * <p>
 */
@Documented
@Retention(SOURCE)
@Target(ANNOTATION_TYPE)
public @interface EventHandler {

}
