package com.solambda.aws.swiffer.api.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.time.Duration;
import java.util.function.Consumer;

import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.solambda.aws.swiffer.api.model.DomainIdentifier;
import com.solambda.aws.swiffer.api.model.TaskType;
import com.solambda.aws.swiffer.api.model.WorkflowTypeId;
import com.solambda.aws.swiffer.api.model.decider.Decider;
import com.solambda.aws.swiffer.api.model.decider.Decisions;
import com.solambda.aws.swiffer.api.model.decider.EventContextHandlerRegistry;
import com.solambda.aws.swiffer.api.model.decider.context.WorkflowStartedContext;
import com.solambda.aws.swiffer.api.model.decider.context.identifier.MarkerName;
import com.solambda.aws.swiffer.api.model.decider.context.identifier.SignalName;
import com.solambda.aws.swiffer.api.model.decider.context.identifier.TimerName;
import com.solambda.aws.swiffer.api.model.decider.handler.WorkflowStartedHandler;

public class ObjectMother {

	private static final String EXISTING_DOMAIN = "domainForTestingSwiffer";
	private static final String DEPRECATED_DOMAIN = "unexisting_domain";
	private static final String NOT_EXISTING_DOMAIN = "notExistingDomain";
	private static EventContextHandlerRegistry mockedHandlerRegistry = mock(EventContextHandlerRegistry.class);

	public static void resetMocks() {
		reset(mockedHandlerRegistry);
	}

	public static EventContextHandlerRegistry mockedRegistry() {
		return mockedHandlerRegistry;
	}

	public static AmazonSimpleWorkflow client() {
		AWSCredentialsProvider awsCredentialsProvider = getProvider();
		return new AmazonSimpleWorkflowClient(awsCredentialsProvider);
	}

	public static AmazonSimpleWorkflow mockedClient() {
		return mock(AmazonSimpleWorkflow.class);
	}

	private static AWSCredentialsProvider getProvider() {
		return new DefaultAWSCredentialsProviderChain();
	}

	public static DomainIdentifier deprecatedDomain() {
		return new DomainIdentifier(DEPRECATED_DOMAIN);
	}

	public static DomainIdentifier notExistingDomain() {
		return new DomainIdentifier(NOT_EXISTING_DOMAIN);
	}

	public static DomainIdentifier domain() {
		return new DomainIdentifier(domainName());
	}

	public static String domainName() {
		return EXISTING_DOMAIN;
	}

	public static WorkflowTypeId registeredWorkflowType() {
		return new WorkflowTypeId(domain(), registeredWorkflowTypeName(), registeredWorkflowTypeVersion());
	}

	public static WorkflowTypeId unregisteredWorkflowType() {
		return new WorkflowTypeId(domain(), "unregistered", "1.0.0");
	}

	public static String registeredWorkflowTypeName() {
		return "swiffer-workflow-test";
	}

	public static WorkflowTypeId smallTimeoutWorkflowType() {
		return new WorkflowTypeId(domain(), smallTimeoutWorkflowTypeName(), smallTimeoutWorkflowTypeVersion());
	}

	public static String smallTimeoutWorkflowTypeName() {
		return "swiffer-workflow-test-timeout";
	}

	public static String registeredWorkflowTypeVersion() {
		return "1.0.0";
	}

	public static String smallTimeoutWorkflowTypeVersion() {
		return "1.0.0";
	}

	public static String smallTimeoutTaskName() {
		return "task-test-timeout";
	}

	public static String smallTimeoutTaskVersion() {
		return "2.0.0";
	}

	public static Duration smallTimeout() {
		return Duration.ofSeconds(10);
	}

	public static Decider completeWorkflowDecider() {
		return (context, decideTo) -> decideTo.completeWorfklow();
	}

	public static Decider cancelWorkflowDecider() {
		return (context, decideTo) -> decideTo.cancelWorfklow();
	}

	public static Decider failWorkflowDecider() {
		return (context, decideTo) -> decideTo.failWorfklow();
	}

	public static void whenWorkflowStarted(final WorkflowStartedHandler handler) {
		Mockito.when(mockedHandlerRegistry.get(any(WorkflowStartedContext.class))).thenReturn(handler);
	}

	public static Stubber makeDecider(final Consumer<Decisions> consumer) {
		return doAnswer(i -> {
			consumer.accept(i.getArgumentAt(1, Decisions.class));
			return null;
		});
	}

	public static String taskName() {
		return "my-task";
	}

	public static String taskVersion() {
		return "2";
	}

	public static String taskDescription() {
		return "my-description";
	}

	public static void sleep(final Duration duration) {
		try {
			Thread.sleep(duration.toMillis());
		} catch (InterruptedException e) {
		}
	}

	public static TaskType taskType() {
		return new TaskType(taskName(), taskVersion());
	}

	public static TaskType smallTimeoutTaskType() {
		return new TaskType(smallTimeoutTaskName(), smallTimeoutTaskVersion());
	}

	public static SignalName signalName() {
		return SignalName.of("signal-name");
	}

	public static TimerName timerName() {
		return TimerName.of("my-timer");
	}

	public static MarkerName markerName() {
		return MarkerName.of("my-marker");
	}

	public static TaskType unregisteredTaskType() {
		return new TaskType("unregistered", "1");
	}

}
