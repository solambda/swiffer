package com.solambda.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.api.Swiffer;
import com.solambda.swiffer.api.WorkflowType;
import com.solambda.swiffer.api.internal.registration.WorkflowRegistry;

@RunWith(Enclosed.class)
@Ignore("should be rewritten")
public class WorkflowRegistryTest {

	private static final String TASK_TOKEN = "token";
	private AmazonSimpleWorkflow swf;
	private Swiffer swiffer;
	private WorkflowTemplate1 workflowTemplate1;
	private static final WorkflowType workflowType = getWorkflowType(WorkflowDef1.class);

	@WorkflowType(name = "workflowType1", version = "1")
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface WorkflowDef1 {

	}

	@WorkflowType(name = "workflowType2", version = "1")
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface WorkflowDef2 {

	}

	@WorkflowDef2
	public static class WorkflowTemplate2 {
		@OnWorkflowStarted
		public void started(final String input) {

		}
	}

	@WorkflowDef1
	public static class WorkflowTemplate1 {
		@OnWorkflowStarted
		public void started(final String input) {

		}
	}

	protected static class BaseTest {
		protected AmazonSimpleWorkflow swf;

		protected WorkflowRegistry registry;
	}

	public static class ARegisteredWorkflow extends BaseTest {

		@Before
		public void setup() {
			// should register the workflow
			this.swf = createSwf();
			this.registry = new WorkflowRegistry(this.swf, "domain");
		}

		@Test
		public void isRegistered() throws Exception {
			assertThat(this.registry.isRegistered(workflowType)).isTrue();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeRegistered() throws Exception {
			this.registry.registerWorkflow(workflowType);
		}
	}

	public static AmazonSimpleWorkflow createSwf() {
		return mock(AmazonSimpleWorkflow.class);
	}

	public static WorkflowType getWorkflowType(final Class<WorkflowDef1> class1) {
		return class1.getAnnotation(WorkflowType.class);
	}

	public static class AnUnregisteredWorkflow extends BaseTest {
		@Before
		public void setup() {
			// should register the workflow
			this.swf = createSwf();
			this.registry = new WorkflowRegistry(this.swf, "domain");
		}

		@Test
		public void doesNotExist() throws Exception {
			assertThat(this.registry.isRegistered(workflowType)).isFalse();
		}

		@Test
		public void canBeRegistered() {

		}
	}

}
