package com.solambda.swiffer.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.decisions.DeciderImpl;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskPoller;
import com.solambda.swiffer.api.internal.decisions.WorkflowTemplate;
import com.solambda.swiffer.api.internal.decisions.WorkflowTemplateFactory;
import com.solambda.swiffer.api.internal.decisions.WorkflowTemplateRegistry;
import com.solambda.swiffer.api.internal.registration.WorkflowTypeRegistry;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.retry.RetryPolicy;

/**
 * A builder of {@link Decider}.
 */
public class DeciderBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeciderBuilder.class);

	private final AmazonSimpleWorkflow swf;
	private final String domain;
	private final WorkflowTypeRegistry workflowTypeRegistry;
	private final DataMapper dataMapper;
	private final DurationTransformer durationTransformer;

	private String identity;
	private String taskList;
	private List<Object> workflowTemplates;
    private RetryPolicy globalRetryPolicy;

	public DeciderBuilder(final AmazonSimpleWorkflow swf, final String domain, DataMapper dataMapper, DurationTransformer durationTransformer) {
		super();
		this.swf = swf;
		this.domain = domain;
		this.dataMapper = dataMapper;
		this.durationTransformer = durationTransformer;

		this.workflowTypeRegistry = new WorkflowTypeRegistry(swf, domain);
	}

	/**
	 * @return a new instance of {@link Decider}
	 */
	public Decider build() {
		final String taskList = this.taskList == null ? "default" : this.taskList;
		final DecisionTaskPoller poller = new DecisionTaskPoller(this.swf, this.domain, taskList, this.identity, dataMapper);
		final WorkflowTemplateRegistry registry = createWorkflowTemplateRegistry();
		return new DeciderImpl(poller, registry);
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

    /**
     * Sets global retry policy for failed or timed out Activities.
     * If not specified then default retry policy will be used.
     * <p>
     * The default retry policy retries Activities with exponentially increasing time between attempts from 5 seconds to 1 hour.
     * The number of retries in unlimited.
     * </p>
     *
     * @param globalRetryPolicy the global retry policy
     * @return this builder
     */
    public DeciderBuilder globalRetryPolicy(RetryPolicy globalRetryPolicy) {
        this.globalRetryPolicy = globalRetryPolicy;
        return this;
    }

    private WorkflowTemplateRegistry createWorkflowTemplateRegistry() {
        WorkflowTemplateFactory templateFactory = new WorkflowTemplateFactory(this.dataMapper, this.durationTransformer, globalRetryPolicy);

        final Map<VersionedName, WorkflowTemplate> registry = new HashMap<>();
        for (final Object workflowTemplate : this.workflowTemplates) {
            final WorkflowTemplate template = templateFactory.createWorkflowTemplate(workflowTemplate);
            ensureWorkflowTypeRegistration(workflowTemplate);
            registry.put(template.getWorkflowType(), template);
        }
        return new WorkflowTemplateRegistry(registry);
    }

    private void ensureWorkflowTypeRegistration(final Object workflowTemplate) {
        final WorkflowType workflowType = WorkflowTemplateFactory.findWorkflowTypeAnnotation(workflowTemplate);
        this.workflowTypeRegistry.registerWorkflowOrCheckConfiguration(workflowType);
    }
}
