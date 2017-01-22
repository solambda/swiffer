/**
 *
 */
package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
/**
 * Configure the registration of an activity.
 * <p>
 */
public @interface ActivityType {

	/**
	 * The registered name of the activity type.
	 *
	 * @return
	 */
	String name();

	/**
	 * The registered version of the activity type.
	 *
	 * @return
	 */
	String version();
}
