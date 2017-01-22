package com.solambda.swiffer.api;

/**
 * A daemon that polls a task list and delegate execution of the tasks to an
 * execution context.
 * <p>
 * The meaning of "executing" depends on the implementation and on the SWF task
 * that is polled: a {@link Worker} will execute activity tasks, whereas a
 * {@link Decider} will execute decision tasks.
 */
public interface TaskListPoller {

	/**
	 * Start polling the task list, and executing the tasks. This methods
	 * returns immediately. It does nothing if the poller is already started.
	 */
	public void start();

	/**
	 * Stop polling. This method does nothing if the poller is not started.
	 * <p>
	 * FIXME: determine the correct behavior according to what it is possible to
	 * do. <br>
	 * This method wait for the current polling operation to finish (which can
	 * take up to 1 minute if no task is available in the task list), but it
	 * does not wait for the completion of the tasks being executed.
	 * <p>
	 *
	 */
	public void stop();

	/**
	 * @return true if the poller is started, false otherwise.
	 */
	public boolean isStarted();

	/**
	 * @return the task list to poll
	 */
	public String getTaskList();

}
