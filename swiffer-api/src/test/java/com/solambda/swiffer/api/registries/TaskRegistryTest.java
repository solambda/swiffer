package com.solambda.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.solambda.swiffer.api.ActivityOptions;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.internal.registration.TaskRegistry;
import com.solambda.swiffer.api.test.ObjectMother;
import com.solambda.swiffer.test.Tests;

@Ignore
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
		final VersionedName taskType = new VersionedName("name", this.version);
		this.registry.create(ObjectMother.domainName(), taskType, "description", new ActivityOptions());
		assertThat(this.registry.exists(ObjectMother.domainName(), taskType)).isTrue();
		this.registry.delete(ObjectMother.domainName(), taskType);
		assertThat(this.registry.exists(ObjectMother.domainName(), taskType)).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void cannotRegisterIfDomainDoesnotExist() throws Exception {
		final VersionedName taskType = new VersionedName("name", "1");
		this.registry.create(Tests.DOMAIN, taskType, "description",
				new ActivityOptions());
	}

	@Test(expected = IllegalStateException.class)
	public void cannotRegisterIfDomainIsDeprecated() throws Exception {
		final VersionedName taskType = new VersionedName("name", "1");
		this.registry.create(Tests.DOMAIN, taskType, "description", new ActivityOptions());
	}

	@Test
	public void existsReturnFalseWhenActivityTypeDoesNotExist() {
		final VersionedName taskType1 = new VersionedName("not_existing", "1");
		final VersionedName taskType2 = new VersionedName("exists", "not_existing_version");
		assertThat(this.registry.exists(ObjectMother.domainName(), taskType1)).isFalse();
		assertThat(this.registry.exists(ObjectMother.domainName(), taskType2)).isFalse();
	}

	@Test
	public void existsReturnTrueWhenActivityTypeExists() {
		final VersionedName taskType = new VersionedName("exists", "1");
		try {
			this.registry.create(ObjectMother.domainName(), taskType, "description", new ActivityOptions());
		} catch (final Exception e) {
		}
		assertThat(this.registry.exists(ObjectMother.domainName(), taskType)).isTrue();
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
