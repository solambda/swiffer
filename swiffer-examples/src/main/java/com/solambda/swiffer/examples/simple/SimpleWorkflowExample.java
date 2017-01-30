package com.solambda.swiffer.examples.simple;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.solambda.swiffer.api.Decider;
import com.solambda.swiffer.api.Swiffer;
import com.solambda.swiffer.api.Worker;
import com.solambda.swiffer.examples.ActivityImplementations;
import com.solambda.swiffer.examples.Domains;
import com.solambda.swiffer.examples.WorkflowDefinitions.SimpleExampleWorkflowDefinition;
import com.solambda.swiffer.examples.templates.SimpleTemplate;

/**
 * Demonstrate usual features of swiffer.
 *
 * The workflow has only one activity: it takes a string and parses it as an
 * integer.
 * <p>
 * <ul>
 * <li>starting a workflow with some input
 * <li>sending signal to the workflow
 * <li>launching a timer
 * <li>completing the workflow successfully
 * <ul>
 *
 */
public class SimpleWorkflowExample {

	private Worker worker;
	private Decider decider;

	public SimpleWorkflowExample() {
	}

	public void run() {
		final Swiffer swiffer = initializeSwiffer();
		createAndStartWorker(swiffer);
		createAndStartDecider(swiffer);
		startWorkflow(swiffer);
	}

	private void startWorkflow(final Swiffer swiffer) {
		swiffer.startWorkflow(SimpleExampleWorkflowDefinition.class, "workflowid");
	}

	private void sendSignal(final Swiffer swiffer) {
	}

	private void createAndStartDecider(final Swiffer swiffer) {
		this.decider = swiffer.newDeciderBuilder()
				.identity(this.getClass().getSimpleName() + "-decider")
				.workflowTemplates(new SimpleTemplate())
				.build();
		this.decider.start();
	}

	private void createAndStartWorker(final Swiffer swiffer) {
		this.worker = swiffer.newWorkerBuilder()
				.taskList("myTaskList")
				.identity(this.getClass().getSimpleName() + "-worker")
				.executors(new ActivityImplementations())
				.build();
		this.worker.start();
	}

	private Swiffer initializeSwiffer() {
		final AmazonSimpleWorkflow amazonSimpleWorkflow = new AmazonSimpleWorkflowClient(
				new DefaultAWSCredentialsProviderChain())
						.withRegion(Regions.EU_WEST_1);
		final Swiffer swiffer = new Swiffer(amazonSimpleWorkflow, Domains.DOMAIN);
		return swiffer;
	}

	public static void main(final String[] args) {
		final SimpleWorkflowExample e = new SimpleWorkflowExample();
		e.run();
	}

}
