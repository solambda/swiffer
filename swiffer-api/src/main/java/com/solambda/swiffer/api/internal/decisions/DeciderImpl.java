package com.solambda.swiffer.api.internal.decisions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.model.AmazonSimpleWorkflowException;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.solambda.swiffer.api.Decider;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.internal.AbstractTaskListService;
import com.solambda.swiffer.api.internal.TaskContextPoller;

public class DeciderImpl extends AbstractTaskListService<DecisionTaskContext> implements Decider {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeciderImpl.class);

	private WorkflowTemplateRegistry registry;
	private DecisionExecutor executor;

	public DeciderImpl(final TaskContextPoller<DecisionTaskContext> poller, final WorkflowTemplateRegistry registry) {
		super(poller);
		this.registry = registry;
		this.executor = new DecisionExecutorImpl(poller.swf());
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
			LOGGER.debug("executing decision task with template {} of workflow {}", template.getClass().getSimpleName(),
					template.getWorkflowType());
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
		} catch (UnknownResourceException ex) {
			//TODO: add more sophisticated error handling?
			LOGGER.error("Cannot make decisions based on the context  " + context, ex);
		} catch (AmazonSimpleWorkflowException ex) {
			switch (ex.getErrorType()) {
				case Client:
					LOGGER.error("SWF Client error for context " + context, ex);
					break;
				case Service:
				case Unknown:
					throw new IllegalStateException("Cannot make decisions based on the context  " + context, ex);
			}
		} catch (final Exception e) {
			// how to recover from that ?
			// use a marker for failure, and externally relaunch ?
			throw new IllegalStateException("Cannot make decisions based on the context  " + context, e);
		}
	}

	@Override
	public void stop() {
		super.stop();
	}

}
