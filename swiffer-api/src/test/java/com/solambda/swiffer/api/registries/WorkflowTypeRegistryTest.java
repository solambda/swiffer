package com.solambda.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.internal.registration.WorkflowTypeRegistry;
import com.solambda.swiffer.test.Tests;

@RunWith(Enclosed.class)
public class WorkflowTypeRegistryTest {

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
		@Ignore("we don't want to register this each time, so this is a one-shot test")
		public void canBeRegistered() {
			assertThat(this.registry.registerWorkflowOrCheckConfiguration(Tests.UNREGISTERED_WORFKLOW_TYPE)).isTrue();
		}
	}

}
