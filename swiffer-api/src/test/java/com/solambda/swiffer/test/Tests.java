package com.solambda.swiffer.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;

import org.mockito.stubbing.Answer;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.WorkflowType;

public class Tests {

	public static final String DOMAIN = "github-swiffer";

	public static <T> Answer<T> returnAfterDelay(final T response, final Duration delay) {

		return (i) -> {
			sleep(delay);
			return response;
		};
	}

	public static <T> Answer<T> failWith(final Throwable e) {

		return (i) -> {
			throw e;
		};
	}

	public static void sleep(final Duration delay) {
		try {
			Thread.sleep(delay.toMillis());
		} catch (final InterruptedException e) {
		}
	}

	public static AmazonSimpleWorkflow swf() {
		return new AmazonSimpleWorkflowClient(new DefaultAWSCredentialsProviderChain())
				.withRegion(Regions.EU_WEST_1);
	}

	@WorkflowType(name = "registered-workflow", version = "2",
			defaultExecutionStartToCloseTimeout = 1000,
			defaultTaskStartToCloseTimeout = 500)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface RegisteredWorkflow {

	}

	@WorkflowType(name = "registered-workflow", version = "2")
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface RegisteredWorkflowWithAnotherConfiguration {

	}

	@WorkflowType(name = "unregistered-workflow", version = "2")
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface UnregisteredWorkflow {

	}

	public static final WorkflowType UNREGISTERED_WORFKLOW_TYPE = getWorkflowType(UnregisteredWorkflow.class);

	public static final WorkflowType REGISTERED_WORFKLOW_TYPE = getWorkflowType(RegisteredWorkflow.class);
	public static final WorkflowType REGISTERED_WORFKLOW_TYPE_WITH_ANOTHER_CONFIGURATION = getWorkflowType(
			RegisteredWorkflowWithAnotherConfiguration.class);

	private static WorkflowType getWorkflowType(final Class<?> class1) {
		return class1.getAnnotation(WorkflowType.class);
	}

	@ActivityType(name = "registered-activity", version = "1",
			defaultTaskStartToCloseTimeout = 500,
			defaultTaskScheduleToStartTimeout = 500,
			defaultTaskPriority = 0,
			defaultTaskScheduleToCloseTimeout = 500,
			defaultTaskHeartbeatTimeout = 500)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface RegisteredActivity {

	}

	@ActivityType(name = "registered-activity", version = "1")
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface RegisteredActivityWithAnotherConfiguration {

	}

	@ActivityType(name = "unregistered-activity", version = "2")
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface UnregisteredActivity {

	}

	public static final ActivityType UNREGISTERED_ACTIVITY_TYPE = getActivityType(UnregisteredActivity.class);

	public static final ActivityType REGISTERED_ACTIVITY_TYPE = getActivityType(RegisteredActivity.class);
	public static final ActivityType REGISTERED_ACTIVITY_TYPE_WITH_ANOTHER_CONFIGURATION = getActivityType(
			RegisteredActivityWithAnotherConfiguration.class);

	private static ActivityType getActivityType(final Class<?> class1) {
		return class1.getAnnotation(ActivityType.class);
	}

}
