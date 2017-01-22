/**
 *
 */
package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * Define the implementation of an activity
 *
 */
public @interface Executor {
	/**
	 * Interface or annotation annotated with {@link ActivityType}.
	 *
	 * @return the activity type this executor implements
	 */
	Class<?> activity();
}
