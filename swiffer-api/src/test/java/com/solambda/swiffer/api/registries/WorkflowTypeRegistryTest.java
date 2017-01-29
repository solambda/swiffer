package com.solambda.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.WorkflowType;
import com.solambda.swiffer.api.internal.registration.WorkflowTypeRegistry;
import com.solambda.swiffer.test.Tests;

@RunWith(Enclosed.class)
public class WorkflowTypeRegistryTest {

	@WorkflowType(name = "workflowType2", version = "1")
	@Retention(RetentionPolicy.RUNTIME)
	private static @interface WorkflowDef2 {

	}

	protected static class BaseTest {
		protected AmazonSimpleWorkflow swf;

		protected WorkflowTypeRegistry registry;
	}

	public static class ARegisteredWorkflow extends BaseTest {

		@Before
		public void setup() {
			// should register the workflow
			this.swf = Tests.swf();
			this.registry = new WorkflowTypeRegistry(this.swf, Tests.DOMAIN);
		}

		@Test
		public void isRegistered() throws Exception {
			assertThat(this.registry.isRegistered(Tests.REGISTERED_WORFKLOW_TYPE)).isTrue();
		}

		@Test
		public void isNotRegisteredAgain() throws Exception {
			assertThat(this.registry.registerWorkflowOrCheckConfiguration(Tests.REGISTERED_WORFKLOW_TYPE)).isFalse();
		}

		@Test(expected = IllegalStateException.class)
		public void checkRegisteredConfigurationFailsForDifferentConfiguration() throws Exception {
			this.registry
					.registerWorkflowOrCheckConfiguration(Tests.REGISTERED_WORFKLOW_TYPE_WITH_ANOTHER_CONFIGURATION);
		}

		@Test
		public void checkRegisteredConfigurationDoesNotFailForSameConfigueration() throws Exception {
			this.registry.registerWorkflowOrCheckConfiguration(Tests.REGISTERED_WORFKLOW_TYPE);
		}
	}

	public static class AnUnregisteredWorkflow extends BaseTest {
		@Before
		public void setup() {
			// should register the workflow
			this.swf = Tests.swf();
			this.registry = new WorkflowTypeRegistry(this.swf, Tests.DOMAIN);
		}

		@Test
		public void doesNotExist() throws Exception {
			assertThat(this.registry.isRegistered(Tests.UNREGISTERED_WORFKLOW_TYPE)).isFalse();
		}

		@Test
		@Ignore
		public void canBeRegistered() {
			assertThat(this.registry.registerWorkflowOrCheckConfiguration(Tests.UNREGISTERED_WORFKLOW_TYPE)).isTrue();
		}
	}

}
