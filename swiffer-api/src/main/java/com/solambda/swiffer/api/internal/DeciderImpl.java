package com.solambda.swiffer.api.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.swiffer.api.Decider;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.decisions.DecisionExecutor;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskContext;
import com.solambda.swiffer.api.internal.decisions.WorkflowTemplate;
import com.solambda.swiffer.api.internal.decisions.WorkflowTemplateRegistry;

public class DeciderImpl extends AbstractTaskListService<DecisionTaskContext> implements Decider {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeciderImpl.class);

	private WorkflowTemplateRegistry registry;
	private DecisionExecutor executor;

	public DeciderImpl(final TaskContextPoller<DecisionTaskContext> poller, final WorkflowTemplateRegistry registry) {
		super(poller);
		this.registry = registry;
	}

	@Override
	protected void executeTask(final DecisionTaskContext task) {
		executeTaskImmediately(task);
	}

	@Override
	protected void executeTaskImmediately(final DecisionTaskContext task) {
		// retrieve the workflow template:
		// retrieve the event handler in the template
		LOGGER.debug("executing decision task {}", task);
		final WorkflowTemplate template = getWorkflowTemplate(task);
		if (template == null) {
			// FATAL issue : how to recover from that ?
			throw new IllegalStateException("Cannot find a workflow template for " + task.workflowType());
		} else {
			execute(task, template);
		}
	}

	private WorkflowTemplate getWorkflowTemplate(final DecisionTaskContext task) {
		return this.registry.get(task.workflowType());
	}

	private void execute(final DecisionTaskContext context,
			final WorkflowTemplate template) {
		try {
			final Decisions decisions = template.decide(context);
			this.executor.apply(context, decisions);
		} catch (final Exception e) {
			// how to recover from that ?
			// use a marker for failure, and externally relaunch ?
			throw new IllegalStateException("Cannot make decisions based on the context  " + context, e);
		}
	}

}
