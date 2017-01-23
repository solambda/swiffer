package com.solambda.swiffer.api.internal.activities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTaskStatus;
import com.amazonaws.services.simpleworkflow.model.RecordActivityTaskHeartbeatRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskCanceledRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskCompletedRequest;
import com.amazonaws.services.simpleworkflow.model.RespondActivityTaskFailedRequest;
import com.google.common.base.Strings;
import com.solambda.swiffer.api.internal.Failure;

public class ActivityExecutionReporterImpl implements ActivityExecutionReporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityExecutionReporterImpl.class);

	private AmazonSimpleWorkflow client;

	public ActivityExecutionReporterImpl(final AmazonSimpleWorkflow client, final String taskToken) {
		super();
		this.client = client;
	}

	@Override
	public void completed(final String taskToken, final String output) {
		LOGGER.debug("task completed: {}, output={}", taskToken, output);
		this.client.respondActivityTaskCompleted(new RespondActivityTaskCompletedRequest()
				.withResult(output)
				.withTaskToken(taskToken));
	}

	@Override
	public void failed(final String taskToken, final Failure failure) {
		LOGGER.debug("task failed: {}, reason={}, details={}", taskToken, failure.reason(), failure.details());
		this.client.respondActivityTaskFailed(new RespondActivityTaskFailedRequest()
				.withTaskToken(taskToken)
				.withReason(Strings.nullToEmpty(failure.reason()))
				.withDetails(failure.details()));
	}

	@Override
	public void progress(final String taskToken, final String details) throws CancelActivityRequested {
		LOGGER.debug("task progress: {}, details={}", taskToken, details);
		final ActivityTaskStatus taskStatus = this.client
				.recordActivityTaskHeartbeat(new RecordActivityTaskHeartbeatRequest()
						.withDetails(details)
						.withTaskToken(taskToken));

		if (taskStatus.getCancelRequested()) {
			throw new CancelActivityRequested();
		}
	}

	@Override
	public void canceled(final String taskToken, final String details) {
		LOGGER.debug("task canceled: {}, details={}", taskToken, details);
		this.client.respondActivityTaskCanceled(new RespondActivityTaskCanceledRequest()
				.withDetails(details)
				.withTaskToken(taskToken));
	}

}
