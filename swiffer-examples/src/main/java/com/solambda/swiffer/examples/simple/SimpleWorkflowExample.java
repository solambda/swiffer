package com.solambda.swiffer.examples.simple;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.solambda.swiffer.api.Decider;
import com.solambda.swiffer.api.Swiffer;
import com.solambda.swiffer.api.Worker;
import com.solambda.swiffer.examples.simple.WorkflowDefinitions.SimpleExampleWorkflowDefinition;

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
		swiffer// .sendToWorkflow("workflowid")
				.sendSignalToWorkflow("workflowid", "name", "input");
	}

	private void createAndStartDecider(final Swiffer swiffer) {
		decider = swiffer.newDeciderBuilder()
				.decisionTaskList("myDecisionTaskList")
				.activityTaskList("myActivityTaskList")
				.identity("myWorker")
				.workflowTemplates(new WorkflowTemplateSimpleExample())
				.build();
		decider.start();
	}

	private void createAndStartWorker(final Swiffer swiffer) {
		worker = swiffer.newWorkerBuilder()
				.taskList("myTaskList")
				.identity("simple-example-worker")
				.executors(new ActivityImplementations())
				.build();
		worker.start();
	}

	private Swiffer initializeSwiffer() {
		final AmazonSimpleWorkflow amazonSimpleWorkflow = new AmazonSimpleWorkflowClient(
				new DefaultAWSCredentialsProviderChain());
		final String domainName = "github-swiffer-example-domain";
		final Swiffer swiffer = new Swiffer(amazonSimpleWorkflow, domainName);
		return swiffer;
	}

	public static void main(final String[] args) {
		final SimpleWorkflowExample e = new SimpleWorkflowExample();
		e.run();
	}

}
