package com.solambda.swiffer.api.internal;

/**
 * Provide a context object by polling a SWF tasklist.
 *
 * @param <T>
 */
public interface TaskContextPoller<T extends TaskContext> {

	/**
	 * Poll the tasklist. This method can be blocking up to 60s if no task is
	 * available in the tasklist.
	 * <p>
	 *
	 * @return a new task context, or null if no task context is available in
	 *         the task list.
	 */
	public abstract T poll();

	/**
	 * Immediately stop polling, making the thread blocked on {@link #poll()}
	 * method to be released.
	 */
	public abstract void stop();

}