package com.solambda.swiffer.api;

import com.amazonaws.services.simpleworkflow.model.ActivityTask;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;

/**
 * A daemon service that polls a task list and delegate execution of the tasks
 * to an execution context.
 * <p>
 * The meaning of "executing" depends on the implementation and on the SWF task
 * that is polled: a {@link Worker} will execute {@link ActivityTask}s, whereas
 * a {@link Decider} will {@link DecisionTask}s .
 */
public interface TaskListService {

	/**
	 * Start the service and returns immediately. Does nothing if the poller is
	 * already started.
	 */
	public void start();

	/**
	 * Stop polling. This method does nothing if the poller is not started.
	 * <p>
	 * This method wait for the current polling operation to finish (which can
	 * take up to 1 minute if no task is available in the task list), and wait
	 * for the completion of the tasks being executed.
	 * <p>
	 *
	 */
	public void stop();

	/**
	 * @return true if the service is started, false otherwise.
	 */
	public boolean isStarted();

}
