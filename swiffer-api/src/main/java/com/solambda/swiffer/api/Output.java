/**
 *
 */
package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to put on a event handler parameter to receive the output of an
 * activity.
 * <p>
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Output {

}
