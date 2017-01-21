package com.solambda.swiffer.api.model.tasks;

import com.solambda.swiffer.api.model.Failure;

public interface TaskReport {

	/**
	 * Report the task has successfully complete
	 * 
	 * @param output
	 *            the output
	 */
	void completed(String output);

	/**
	 * Report the task has failed
	 * 
	 * @param error
	 *            the failure to report
	 */
	void failed(Failure failure);

	/**
	 * Report progress on the task
	 * 
	 * @param details
	 *            the details of the progress
	 * @throws CancelRequested
	 *             if the task has been requested to cancel externally
	 */
	void progress(String details) throws CancelRequested;

	/**
	 * @param details
	 * @throws IllegalStateException
	 *             if the task has not been requested to cancel externally
	 */
	void canceled(String details);
}
