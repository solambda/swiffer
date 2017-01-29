package com.solambda.swiffer.api.internal.registration;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DescribeWorkflowTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterWorkflowTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RegistrationStatus;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.amazonaws.services.simpleworkflow.model.TypeAlreadyExistsException;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.amazonaws.services.simpleworkflow.model.WorkflowTypeConfiguration;
import com.amazonaws.services.simpleworkflow.model.WorkflowTypeDetail;
import com.amazonaws.services.simpleworkflow.model.WorkflowTypeInfo;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.WorkflowType;
import com.solambda.swiffer.api.internal.SwfAware;

public class WorkflowRegistry implements SwfAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRegistry.class);

	private static final int ONE_YEAR = 365 * 24 * 60 * 60;

	private AmazonSimpleWorkflow swf;
	private String domain;

	public WorkflowRegistry(final AmazonSimpleWorkflow swf, final String domain) {
		super();
		this.swf = swf;
		this.domain = domain;
	}

	public boolean isRegistered(final WorkflowType workflowType) {
		final WorkflowTypeDetail detail = getWorkflowTypeDetail(workflowType);
		return detail != null
				&& detail.getTypeInfo() != null
				&& Objects.equals(detail.getTypeInfo().getStatus(), RegistrationStatus.REGISTERED.name());
	}

	private WorkflowTypeDetail getWorkflowTypeDetail(final WorkflowType workflowType) {
		try {
			final WorkflowTypeDetail detail = this.swf.describeWorkflowType(
					new DescribeWorkflowTypeRequest()
							.withDomain(this.domain)
							.withWorkflowType(toWorkflowType(workflowType)));
			return detail;
		} catch (final UnknownResourceException e) {
			return null;
		}
	}

	private com.amazonaws.services.simpleworkflow.model.WorkflowType toWorkflowType(final WorkflowType identifier) {
		return new com.amazonaws.services.simpleworkflow.model.WorkflowType()
				.withName(identifier.name())
				.withVersion(identifier.version());
	}

	public void registerWorkflow(final WorkflowType workflowType) {
		final WorkflowTypeDetail detail = getWorkflowTypeDetail(workflowType);
		if (detail != null) {
			ensureRegisteredAndSpecifedConfigurationsAreTheSame(detail, workflowType);
		} else {
			doWorkflowTypeRegistration(workflowType);
		}
	}

	private void doWorkflowTypeRegistration(final WorkflowType workflowType) {
		try {
			LOGGER.debug("Registering workflow {}", workflowType);
			this.swf.registerWorkflowType(new RegisterWorkflowTypeRequest()
					// identification of the WF
					.withName(workflowType.name())
					.withVersion(workflowType.version())
					.withDomain(this.domain)
					// description
					.withDescription(workflowType.description())

					// defaults behaviors
					.withDefaultChildPolicy(workflowType.defaultChildPolicy())
					.withDefaultTaskList(taskList(workflowType))
					.withDefaultTaskPriority(taskPriority(workflowType))
					.withDefaultExecutionStartToCloseTimeout(executionTimeout(workflowType))
					.withDefaultTaskStartToCloseTimeout(taskTimeout(workflowType)));

		} catch (final TypeAlreadyExistsException e) {
			throw new IllegalStateException("should never occurs", e);
		}
	}

	private String taskPriority(final WorkflowType workflowType) {
		return Integer.toString(workflowType.defaultTaskPriority());
	}

	private TaskList taskList(final WorkflowType workflowType) {
		return new TaskList().withName(workflowType.defaultTaskList());
	}

	private String taskTimeout(final WorkflowType workflowType) {
		final int timeout = workflowType.defaultTaskStartToCloseTimeout();
		return timeout >= 0 ? Integer.toString(timeout) : "NONE";
	}

	private String executionTimeout(final WorkflowType workflowType) {
		final int timeout = workflowType.defaultExecutionStartToCloseTimeout();
		Preconditions.checkArgument(timeout >= 0,
				"defaultExecutionStartToCloseTimeout must be a positive integer");
		return Integer.toString(timeout == Integer.MAX_VALUE ? ONE_YEAR : timeout);
	}

	private void ensureRegisteredAndSpecifedConfigurationsAreTheSame(
			final WorkflowTypeDetail registeredDetail,
			final WorkflowType workflowType) {

		final WorkflowTypeDetail specifiedDetail = toWorkflowTypeDetail(workflowType);
		registeredDetail.getTypeInfo()
				.withCreationDate(null)
				.withDeprecationDate(null);
		//
		final String message = String.format(
				"The workflow type %s is already registered but with a different configuration than the one specified. "
						+ "Currently registered configuration: %s. "
						+ "Specified configuration: %s",
				workflowType, registeredDetail, specifiedDetail);
		Preconditions.checkState(Objects.equals(specifiedDetail, registeredDetail), message);
	}

	private WorkflowTypeDetail toWorkflowTypeDetail(final WorkflowType workflowType) {
		final WorkflowTypeInfo specifiedInfo = new WorkflowTypeInfo()
				.withDescription(workflowType.description())
				.withStatus(RegistrationStatus.REGISTERED)
				.withWorkflowType(toWorkflowType(workflowType));
		final WorkflowTypeConfiguration specifiedConfiguration = new WorkflowTypeConfiguration()
				.withDefaultChildPolicy(workflowType.defaultChildPolicy())
				.withDefaultExecutionStartToCloseTimeout(
						executionTimeout(workflowType))
				.withDefaultLambdaRole(workflowType.defaultLambdaRole())
				.withDefaultTaskList(taskList(workflowType))
				.withDefaultTaskPriority(taskPriority(workflowType))
				.withDefaultTaskStartToCloseTimeout(taskTimeout(workflowType));
		return new WorkflowTypeDetail()
				.withConfiguration(specifiedConfiguration)
				.withTypeInfo(specifiedInfo);
	}

	@Override
	public AmazonSimpleWorkflow swf() {
		return this.swf;
	}

	@Override
	public String domain() {
		return this.domain;
	}

}
