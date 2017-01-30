package com.solambda.swiffer.examples.simple;

import java.time.Duration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.solambda.swiffer.api.Decider;
import com.solambda.swiffer.api.Swiffer;
import com.solambda.swiffer.api.Worker;
import com.solambda.swiffer.examples.ActivityImplementations;
import com.solambda.swiffer.examples.Domains;
import com.solambda.swiffer.examples.WorkflowDefinitions;
import com.solambda.swiffer.examples.WorkflowDefinitions.SimpleExampleWorkflowDefinition;
import com.solambda.swiffer.examples.templates.SimpleTemplate;
import com.solambda.swiffer.examples.utils.Tests;

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
public class SimpleTemplateExample {

	private static final String WORKFLOW_ID = "workflowid";
	private Worker worker;
	private Decider decider;

	public SimpleTemplateExample() {
	}

	public void run() {
		final Swiffer swiffer = initializeSwiffer();
		createAndStartWorker(swiffer);
		createAndStartDecider(swiffer);
		startWorkflow(swiffer);
		Tests.sleep(Duration.ofSeconds(7));
		sendSignal(swiffer);
		Tests.sleep(Duration.ofSeconds(3));
		stopWorkerAndDecider();
	}

	private void stopWorkerAndDecider() {
		this.decider.stop();
		this.worker.stop();
	}

	private void startWorkflow(final Swiffer swiffer) {
		final String stringToParse = "123";
		swiffer.startWorkflow(SimpleExampleWorkflowDefinition.class, WORKFLOW_ID, stringToParse);
	}

	private void sendSignal(final Swiffer swiffer) {
		swiffer.sendSignalToWorkflow(WORKFLOW_ID, WorkflowDefinitions.SIGNAL_NAME, "signalInput");
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
				.taskList("default")
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
		final SimpleTemplateExample e = new SimpleTemplateExample();
		e.run();
	}

}
