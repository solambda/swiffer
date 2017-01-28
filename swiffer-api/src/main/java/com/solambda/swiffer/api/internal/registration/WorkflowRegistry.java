package com.solambda.swiffer.api.internal.registration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;

public class WorkflowRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRegistry.class);

	private AmazonSimpleWorkflow client;
	private String domain;

	public WorkflowRegistry(final AmazonSimpleWorkflow client, final String domain) {
		super();
		this.client = client;
		this.domain = domain;
	}
	//
	// public boolean exists(final WorkflowType identifier) {
	// try {
	// final WorkflowType workflowType = toWorkflowType(identifier);
	//
	// final WorkflowTypeDetail detail = this.client.describeWorkflowType(
	// new
	// DescribeWorkflowTypeRequest().withDomain(identifier.domainId().getName()).withWorkflowType(
	// workflowType));
	// final WorkflowTypeInfo typeInfo = detail.getTypeInfo();
	// return typeInfo != null &&
	// typeInfo.getWorkflowType().equals(workflowType);
	// } catch (final UnknownResourceException e) {
	// return false;
	// }
	// }
	//
	// private WorkflowType toWorkflowType(final WorkflowType identifier) {
	// return new WorkflowType()
	// .withName(identifier.name())
	// .withVersion(identifier.version());
	// }
	////
	// public void registerWorkflow(final WorkflowTypeId identifier, final
	// String description,
	// final WorkflowOptions defaultConfiguration) {
	// try {
	// LOGGER.debug("registering workflow {}", identifier);
	// final String defaultMaxExecutionDuration =
	// defaultConfiguration.getMaxExecutionDuration();
	// final String defaultMaxTaskDuration =
	// defaultConfiguration.getMaxDecisionTaskDuration();
	// this.client.registerWorkflowType(new RegisterWorkflowTypeRequest()
	// // identification of the WF
	// .withName(identifier.name())
	// .withVersion(identifier.version())
	// .withDomain(identifier.domainId().getName())
	// // description
	// .withDescription(description)
	//
	// // defaults behaviors
	// .withDefaultChildPolicy(defaultConfiguration.getChildTerminationPolicy())
	// .withDefaultTaskList(
	// defaultConfiguration.getTaskListIdentifier() == null ? null
	// : new TaskList().withName(defaultConfiguration.getTaskListIdentifier()
	// .getName()))
	// .withDefaultTaskPriority(defaultConfiguration.getTaskPriority())
	//
	// .withDefaultExecutionStartToCloseTimeout(defaultMaxExecutionDuration)
	// .withDefaultTaskStartToCloseTimeout(defaultMaxTaskDuration));
	// // /////
	// // register definition
	// // registerDefinition(workflow, definition);
	// } catch (final TypeAlreadyExistsException e) {
	// throw new IllegalStateException("cannot register the workflow " +
	// identifier, e);
	// } catch (final UnknownResourceException e) {
	// throw new IllegalStateException("cannot check for workflow existence",
	// e);
	// }
	// }

	// public void registerWorkflow(final WorkflowTypeId identifier) {
	// registerWorkflow(identifier, null, new
	// WorkflowOptions().maxWorkflowDuration(Duration.ofSeconds(30)));
	// }
	//
	// public void unregisterWorkflow(final WorkflowTypeId identifier) {
	// try {
	// this.client.deprecateWorkflowType(
	// new
	// DeprecateWorkflowTypeRequest().withDomain(identifier.domainId().getName())
	// .withWorkflowType(toWorkflowType(identifier)));
	// } catch (TypeDeprecatedException | UnknownResourceException e) {
	// throw new IllegalStateException("cannot unregister workflow " +
	// identifier, e);
	// }
	// }

}
