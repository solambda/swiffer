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
 * Annotation to put on a event handler parameter to receive the reason of a
 * failed or timed out activity, or a terminated workflow.
 * <p>
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Reason {

}
