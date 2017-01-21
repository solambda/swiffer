package com.solambda.swiffer.api.registries;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.*;
import com.solambda.swiffer.api.model.Options;
import com.solambda.swiffer.api.model.WorkflowTypeId;

public class WorkflowRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRegistry.class);

	private AmazonSimpleWorkflow client;

	public WorkflowRegistry(final AmazonSimpleWorkflow client) {
		super();
		this.client = client;
	}

	public boolean exists(final WorkflowTypeId identifier) {
		try {
			WorkflowType workflowType = toWorkflowType(identifier);

			WorkflowTypeDetail detail = client.describeWorkflowType(
					new DescribeWorkflowTypeRequest().withDomain(identifier.domainId().getName()).withWorkflowType(
							workflowType));
			WorkflowTypeInfo typeInfo = detail.getTypeInfo();
			return typeInfo != null && typeInfo.getWorkflowType().equals(workflowType);
		} catch (UnknownResourceException e) {
			return false;
		}
	}

	private WorkflowType toWorkflowType(final WorkflowTypeId identifier) {
		return new WorkflowType()
				.withName(identifier.name())
				.withVersion(identifier.version());
	}

	public void registerWorkflow(final WorkflowTypeId identifier, final String description, final Options defaultConfiguration) {
		try {
			LOGGER.debug("registering workflow {}", identifier);
			String defaultMaxExecutionDuration = defaultConfiguration.getMaxExecutionDuration();
			String defaultMaxTaskDuration = defaultConfiguration.getMaxDecisionTaskDuration();
			client.registerWorkflowType(new RegisterWorkflowTypeRequest()
					// identification of the WF
					.withName(identifier.name())
					.withVersion(identifier.version())
					.withDomain(identifier.domainId().getName())
					// description
					.withDescription(description)

					// defaults behaviors
					.withDefaultChildPolicy(defaultConfiguration.getChildTerminationPolicy())
					.withDefaultTaskList(
							defaultConfiguration.getTaskListIdentifier() == null ? null : new TaskList().withName(defaultConfiguration.getTaskListIdentifier()
									.getName()))
					.withDefaultTaskPriority(defaultConfiguration.getTaskPriority())

					.withDefaultExecutionStartToCloseTimeout(defaultMaxExecutionDuration)
					.withDefaultTaskStartToCloseTimeout(defaultMaxTaskDuration));
			// /////
			// register definition
			// registerDefinition(workflow, definition);
		} catch (TypeAlreadyExistsException e) {
			throw new IllegalStateException("cannot register the workflow " + identifier, e);
		} catch (UnknownResourceException e) {
			throw new IllegalStateException("cannot check for workflow existence", e);
		}
	}

	public void registerWorkflow(final WorkflowTypeId identifier) {
		registerWorkflow(identifier, null, Options.maxWorkflowDuration(Duration.ofSeconds(30)));
	}

	public void unregisterWorkflow(final WorkflowTypeId identifier) {
		try {
			client.deprecateWorkflowType(new DeprecateWorkflowTypeRequest().withDomain(identifier.domainId().getName())
					.withWorkflowType(toWorkflowType(identifier)));
		} catch (TypeDeprecatedException | UnknownResourceException e) {
			throw new IllegalStateException("cannot unregister workflow " + identifier, e);
		}
	}

}
