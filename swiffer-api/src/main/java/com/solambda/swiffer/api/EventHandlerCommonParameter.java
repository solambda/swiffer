/**
 *
 */
package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A documentation purpose annotation used to denote the parameters that an
 * {@link EventHandler} can accept.
 * <p>
 * Any class or annotation annotated with this annotation can be used as a
 * parameter for any Event handler.
 */
@Documented
@Retention(SOURCE)
@Target({ TYPE, ANNOTATION_TYPE })
public @interface EventHandlerCommonParameter {

}
