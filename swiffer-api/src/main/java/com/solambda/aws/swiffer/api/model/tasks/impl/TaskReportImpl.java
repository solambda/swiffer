package com.solambda.aws.swiffer.api.model.tasks.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.*;
import com.solambda.aws.swiffer.api.model.Failure;
import com.solambda.aws.swiffer.api.model.tasks.CancelRequested;
import com.solambda.aws.swiffer.api.model.tasks.TaskReport;
import com.google.common.base.Strings;

public class TaskReportImpl implements TaskReport {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReportImpl.class);

	private AmazonSimpleWorkflow client;
	private String taskToken;

	public TaskReportImpl(final AmazonSimpleWorkflow client, final String taskToken) {
		super();
		this.client = client;
		this.taskToken = taskToken;
	}

	@Override
	public void completed(final String output) {
		LOGGER.debug("task completed: {}, output={}", taskToken, output);
		client.respondActivityTaskCompleted(new RespondActivityTaskCompletedRequest()
				.withResult(output)
				.withTaskToken(taskToken)
				);
	}

	@Override
	public void failed(final Failure failure) {
		LOGGER.debug("task failed: {}, reason={}, details={}", taskToken, failure.reason(), failure.details());
		client.respondActivityTaskFailed(new RespondActivityTaskFailedRequest()
				.withTaskToken(taskToken)
				.withReason(Strings.nullToEmpty(failure.reason()))
				.withDetails(failure.details()));
	}

	@Override
	public void progress(final String details) throws CancelRequested {
		LOGGER.debug("task progress: {}, details={}", taskToken, details);
		ActivityTaskStatus taskStatus = client.recordActivityTaskHeartbeat(new RecordActivityTaskHeartbeatRequest()
				.withDetails(details)
				.withTaskToken(taskToken)
				);

		if (taskStatus.getCancelRequested()) {
			throw new CancelRequested();
		}
	}

	@Override
	public void canceled(final String details) {
		LOGGER.debug("task canceled: {}, details={}", taskToken, details);
		client.respondActivityTaskCanceled(new RespondActivityTaskCanceledRequest()
				.withDetails(details)
				.withTaskToken(taskToken)
				);
	}

}
