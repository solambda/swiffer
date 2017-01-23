package com.solambda.swiffer.api.model;

import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.solambda.swiffer.api.WorkflowOptions;
import com.solambda.swiffer.api.internal.Failure;

/**
 * Represents a particular execution of a workflow or a future execution of a
 * workflow, and provides operations to manage it.
 * <p>
 * A workflow {@link #exists()} if there is at least one execution that is
 * started or closed, or if
 * <p>
 * A workflow have an id and a runId: the runId can be obtains from visibility
 * methods of {@link WorkflowBuilder} or by calling start().
 * <p>
 * Available operations on the worklfow are {@link #requestCancel()},
 * {@link #signal(String)}, and {@link #terminate()}.
 * <p>
 *
 */
public interface Workflow {

	public String start();

	public String start(String input);

	public String start(WorkflowOptions options);

	public String start(String input, WorkflowOptions options);

	// P2 : tags handling and visibility
	// public String start(Tags tags);
	// public String start(String input, Tags tags);
	// public void start(options, tags);
	// public void start(input, options, tags);

	// public boolean isStarted(String runId);

	public void terminate();

	public void terminate(ChildPolicy childTerminationPolicy);

	public void terminate(Failure failure);

	public void terminate(Failure failure, ChildPolicy childTerminationPolicy);

	/**
	 * Request the workflow to be cancelled. It is up to the decider to decide
	 * to actually gracefully cancel the workflow.
	 * <p>
	 */
	public void requestCancel();

	// public void cancel(String runId);

	public void signal(String signalName);

	public void signal(String signalName, String input);

	/**
	 * @return true if the workflow is started, false otherwise
	 */
	public boolean isStarted();

	/**
	 * @return true if the workflow is closed
	 */
	public boolean isClosed();

	/**
	 * @return true if the workflow is closed with timeout
	 * @throws IllegalStateException
	 *             if this object does not represent a particular execution of
	 *             the workflow
	 */
	public boolean isTimedOut();

	/**
	 * @return true if the workflow is canceled
	 * @throws IllegalStateException
	 *             if this object does not represent a particular execution of
	 *             the workflow
	 */
	public boolean isCanceled();

	/**
	 * @return true if the workflow is terminated
	 * @throws IllegalStateException
	 *             if this object does not represent a particular execution of
	 *             the workflow
	 */
	public boolean isTerminated();

	/**
	 * @return true if the workflow is failed
	 * @throws IllegalStateException
	 *             if this object does not represent a particular execution of
	 *             the workflow
	 */
	public boolean isFailed();

	/**
	 * @return true if the workflow is succesfully complete
	 * @throws IllegalStateException
	 *             if this object does not represent a particular execution of
	 *             the workflow
	 */
	public boolean isComplete();

	/**
	 * @return true if the workflow has been continued as a new execution
	 * @throws IllegalStateException
	 *             if this object does not represent a particular execution of
	 *             the workflow
	 */
	public boolean isContinued();

	/**
	 * @return true if this workflow object represents a particular execution,
	 *         false otherwise
	 */
	public boolean isExecution();

	/**
	 * Wait for the workflow to close.
	 * 
	 * @throws IllegalStateException
	 *             if this object does not represent a particular execution of
	 *             the workflow or it the {@link Thread} is interrupted
	 */
	public void awaitClose();
}
