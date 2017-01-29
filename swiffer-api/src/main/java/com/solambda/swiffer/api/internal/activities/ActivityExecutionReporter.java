package com.solambda.swiffer.api.internal.activities;

import com.solambda.swiffer.api.exceptions.CancelActivityRequested;
import com.solambda.swiffer.api.internal.Failure;

public interface ActivityExecutionReporter {

	/**
	 * Report the task has successfully complete
	 *
	 * @param output
	 *            the output
	 */
	void completed(String taskToken, String output);

	/**
	 * Report the task has failed
	 *
	 * @param error
	 *            the failure to report
	 */
	void failed(String taskToken, Failure failure);

	/**
	 * Report progress on the task
	 *
	 * @param details
	 *            the details of the progress
	 * @throws CancelActivityRequested
	 *             if the task has been requested to cancel externally
	 */
	void progress(String taskToken, String details) throws CancelActivityRequested;

	/**
	 * @param details
	 * @throws IllegalStateException
	 *             if the task has not been requested to cancel externally
	 */
	void canceled(String taskToken, String details);
}
