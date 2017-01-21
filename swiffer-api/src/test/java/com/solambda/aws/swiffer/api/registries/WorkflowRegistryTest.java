package com.solambda.aws.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.solambda.aws.swiffer.api.model.WorkflowTypeId;
import com.solambda.aws.swiffer.api.test.ObjectMother;

public class WorkflowRegistryTest {

	public static class ARegisteredWorkflow {

		@Test
		public void exists() throws Exception {
			WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
			assertThat(registry.exists(ObjectMother.registeredWorkflowType())).isTrue();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeRegistered() throws Exception {
			WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
			registry.registerWorkflow(ObjectMother.registeredWorkflowType());
		}
	}

	public static class AnUnregisteredWorkflow {

		@Test
		public void doesNotExist() throws Exception {
			WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
			assertThat(registry.exists(ObjectMother.unregisteredWorkflowType())).isFalse();
		}

		@Test(expected = IllegalStateException.class)
		public void cannotBeUnregistered() throws Exception {
			WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
			registry.unregisterWorkflow(ObjectMother.unregisteredWorkflowType());
		}

		public static class InADeprecatedDomain {
			private WorkflowTypeId id = new WorkflowTypeId(ObjectMother.deprecatedDomain(), "anlt", "blag");

			@Test(expected = IllegalStateException.class)
			public void cannotBeRegistered() throws Exception {
				WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
				registry.registerWorkflow(id);
			}

			@Test(expected = IllegalStateException.class)
			public void cannotBeUnregistered() throws Exception {
				WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
				registry.unregisterWorkflow(id);
			}

			@Test
			public void doesNotExist() throws Exception {
				WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
				registry.exists(id);
			}
		}

		public static class InAnUnregisteredDomain {
			private WorkflowTypeId id = new WorkflowTypeId(ObjectMother.notExistingDomain(), "anlt", "blag");

			@Test(expected = IllegalStateException.class)
			public void cannotBeRegistered() throws Exception {
				WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
				registry.registerWorkflow(id);
			}

			@Test(expected = IllegalStateException.class)
			public void cannotBeUnregistered() throws Exception {
				WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
				registry.unregisterWorkflow(id);
			}

			@Test
			public void doesNotExist() throws Exception {
				WorkflowRegistry registry = new WorkflowRegistry(ObjectMother.client());
				registry.exists(id);
			}
		}
	}

}
