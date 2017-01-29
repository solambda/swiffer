package com.solambda.swiffer.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
/**
 * Configure the registration of a workflow type.
 * <p>
 */
public @interface WorkflowType {
	/**
	 * The registered name of the workflow type.
	 *
	 * @return
	 */
	String name();

	/**
	 * The registered version of the workflow type.
	 *
	 * @return
	 */
	String version();
}
