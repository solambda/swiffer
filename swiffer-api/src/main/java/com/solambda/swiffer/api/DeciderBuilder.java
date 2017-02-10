package com.solambda.swiffer.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.decisions.DeciderImpl;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskPoller;
import com.solambda.swiffer.api.internal.decisions.WorkflowTemplate;
import com.solambda.swiffer.api.internal.decisions.WorkflowTemplateFactory;
import com.solambda.swiffer.api.internal.decisions.WorkflowTemplateRegistry;
import com.solambda.swiffer.api.internal.registration.WorkflowTypeRegistry;
import com.solambda.swiffer.api.mapper.DataMapper;

/**
 * A builder of {@link Decider}.
 */
public class DeciderBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeciderBuilder.class);

	private AmazonSimpleWorkflow swf;
	private String domain;
	private String identity;
	private String taskList;
	private List<Object> workflowTemplates;
	private WorkflowTemplateFactory templateFactory;
	private WorkflowTypeRegistry workflowTypeRegistry;
	private final DataMapper dataMapper;

	public DeciderBuilder(final AmazonSimpleWorkflow swf, final String domain, DataMapper dataMapper) {
		super();
		this.swf = swf;
		this.domain = domain;
		this.dataMapper = dataMapper;
		this.templateFactory = new WorkflowTemplateFactory(this.dataMapper);
		this.workflowTypeRegistry = new WorkflowTypeRegistry(swf, domain);
	}

	/**
	 * @return a new instance of {@link Decider}
	 */
	public Decider build() {
		final String taskList = this.taskList == null ? "default" : this.taskList;
		final DecisionTaskPoller poller = new DecisionTaskPoller(this.swf, this.domain, taskList, this.identity);
		final WorkflowTemplateRegistry registry = createWorkflowTemplateRegistry();
		return new DeciderImpl(poller, registry);
	}

	private WorkflowTemplateRegistry createWorkflowTemplateRegistry() {
		final Map<VersionedName, WorkflowTemplate> registry = new HashMap<>();
		for (final Object workflowTemplate : this.workflowTemplates) {
			final WorkflowTemplate template = this.templateFactory.createWorkflowTemplate(workflowTemplate);
			ensureWorkflowTypeRegistration(workflowTemplate);
			registry.put(template.getWorkflowType(), template);
		}
		return new WorkflowTemplateRegistry(registry);
	}

	private void ensureWorkflowTypeRegistration(final Object workflowTemplate) {
		final WorkflowType workflowType = WorkflowTemplateFactory.findWorkflowTypeAnnotation(workflowTemplate);
		this.workflowTypeRegistry.registerWorkflowOrCheckConfiguration(workflowType);
	}

	/**
	 * @param taskList
	 *            the task list to poll for decision tasks
	 * @return this builder
	 */
	public DeciderBuilder taskList(final String taskList) {
		this.taskList = taskList;
		return this;
	}

	/**
	 * Optional name of the decider
	 *
	 * @param identity
	 *            name of the Decider, for information
	 * @return this builder
	 */
	public DeciderBuilder identity(final String identity) {
		this.identity = identity;
		return this;
	}

	/**
	 * Required
	 *
	 * @param workflowTemplates
	 *            the templates that should handle business logic of polled
	 *            decision tasks
	 * @return this builder
	 */
	public DeciderBuilder workflowTemplates(final Object... workflowTemplates) {
		this.workflowTemplates = Arrays.asList(workflowTemplates);
		return this;
	}
}
