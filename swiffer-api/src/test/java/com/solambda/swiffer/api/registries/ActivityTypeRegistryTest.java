package com.solambda.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.internal.registration.ActivityTypeRegistry;
import com.solambda.swiffer.test.Tests;

@RunWith(Enclosed.class)
public class ActivityTypeRegistryTest {

	protected static class BaseTest {
		protected AmazonSimpleWorkflow swf;

		protected ActivityTypeRegistry registry;
	}

	public static class ARegisteredActivity extends BaseTest {

		@Before
		public void setup() {
			// should register the activity
			this.swf = Tests.swf();
			this.registry = new ActivityTypeRegistry(this.swf, Tests.DOMAIN);
		}

		@Test
		public void isRegistered() throws Exception {
			assertThat(this.registry.isRegistered(Tests.REGISTERED_ACTIVITY_TYPE)).isTrue();
		}

		@Test
		public void isNotRegisteredAgain() throws Exception {
			assertThat(this.registry.registerActivityOrCheckConfiguration(Tests.REGISTERED_ACTIVITY_TYPE)).isFalse();
		}

		@Test(expected = IllegalStateException.class)
		public void checkRegisteredConfigurationFailsForDifferentConfiguration() throws Exception {
			this.registry
					.registerActivityOrCheckConfiguration(Tests.REGISTERED_ACTIVITY_TYPE_WITH_ANOTHER_CONFIGURATION);
		}

		@Test
		public void checkRegisteredConfigurationDoesNotFailForSameConfigueration() throws Exception {
			this.registry.registerActivityOrCheckConfiguration(Tests.REGISTERED_ACTIVITY_TYPE);
		}
	}

	public static class AnUnregisteredActivity extends BaseTest {
		@Before
		public void setup() {
			// should register the activity
			this.swf = Tests.swf();
			this.registry = new ActivityTypeRegistry(this.swf, Tests.DOMAIN);
		}

		@Test
		public void doesNotExist() throws Exception {
			assertThat(this.registry.isRegistered(Tests.UNREGISTERED_ACTIVITY_TYPE)).isFalse();
		}

		@Test
		@Ignore("we don't want to register this each time, so this is a one-shot test")
		public void canBeRegistered() {
			assertThat(this.registry.registerActivityOrCheckConfiguration(Tests.UNREGISTERED_ACTIVITY_TYPE)).isTrue();
		}
	}

}
