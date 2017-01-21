package com.solambda.aws.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.solambda.aws.swiffer.api.model.TaskType;
import com.solambda.aws.swiffer.api.model.tasks.TaskOptions;
import com.solambda.aws.swiffer.api.test.ObjectMother;

public class TaskRegistryTest {

	private TaskRegistry registry;

	private String version = Long.toString(Instant.now().getEpochSecond());

	@Before
	public void createRegistry() {
		this.registry = new TaskRegistry(ObjectMother.client());
	}

	@Test
	@Ignore("do not exceeds the workflow limits")
	public void createAndDeleteWorksAsExpected() throws Exception {
		TaskType taskType = new TaskType("name", version);
		registry.create(ObjectMother.domainName(), taskType, "description", new TaskOptions());
		assertThat(registry.exists(ObjectMother.domainName(), taskType)).isTrue();
		registry.delete(ObjectMother.domainName(), taskType);
		assertThat(registry.exists(ObjectMother.domainName(), taskType)).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void cannotRegisterIfDomainDoesnotExist() throws Exception {
		TaskType taskType = new TaskType("name", "1");
		registry.create(ObjectMother.notExistingDomain().getName(), taskType, "description", new TaskOptions());
	}

	@Test(expected = IllegalStateException.class)
	public void cannotRegisterIfDomainIsDeprecated() throws Exception {
		TaskType taskType = new TaskType("name", "1");
		registry.create(ObjectMother.deprecatedDomain().getName(), taskType, "description", new TaskOptions());
	}

	@Test
	public void existsReturnFalseWhenActivityTypeDoesNotExist() {
		TaskType taskType1 = new TaskType("not_existing", "1");
		TaskType taskType2 = new TaskType("exists", "not_existing_version");
		assertThat(registry.exists(ObjectMother.domainName(), taskType1)).isFalse();
		assertThat(registry.exists(ObjectMother.domainName(), taskType2)).isFalse();
	}

	@Test
	public void existsReturnTrueWhenActivityTypeExists() {
		TaskType taskType = new TaskType("exists", "1");
		try {
			registry.create(ObjectMother.domainName(), taskType, "description", new TaskOptions());
		} catch (Exception e) {
		}
		assertThat(registry.exists(ObjectMother.domainName(), taskType)).isTrue();
	}

	@Test
	@Ignore("P2 not specified")
	public void readReturnNullWhenActivityTypeIsDeprecated() {
		throw new IllegalStateException("not specified");
	}

	@Test
	@Ignore("P2 not specified")
	public void readReturnNullWhenActivityTypeDoesNotExist() {
		throw new IllegalStateException("not specified");
	}

	@Test
	@Ignore("P2 not specified")
	public void readReturnsTheConfigurationWhenActivityTypeExists() {
		throw new IllegalStateException("not specified");
	}
}
