package com.solambda.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Period;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.internal.domains.DomainConfiguration;
import com.solambda.swiffer.api.internal.domains.DomainIdentifier;
import com.solambda.swiffer.api.internal.registration.DomainRegistry;
import com.solambda.swiffer.api.test.ObjectMother;

@Ignore
public class DomainRegistryTest {

	private DomainRegistry registry;

	@Before
	public void createRegistry() {
		final AmazonSimpleWorkflow client = ObjectMother.client();
		this.registry = new DomainRegistry(client);
	}

	@Test
	public void createWorksAsExpected() throws Exception {
		// we wont do that, since domain are persisted for eternity...
		this.registry.createDomain(new DomainIdentifier(ObjectMother.domainName()),
				new DomainConfiguration("Domain for testing Swiffer lib", Period.ofDays(1)));
	}

	@Test
	public void existsReturnFalseWhenDomainIsDeprecated() {
		assertThat(this.registry.domainExists(ObjectMother.deprecatedDomain())).isFalse();
	}

	@Test
	public void existsReturnFalseWhenDomainDoesNotExist() {
		assertThat(this.registry.domainExists(ObjectMother.notExistingDomain())).isFalse();
	}

	@Test
	public void existsReturnTrueWhenDomainExist() {
		assertThat(this.registry.domainExists(ObjectMother.domain())).isTrue();
	}

	@Test
	public void readReturnNullWhenDomainIsDeprecated() {
		assertThat(this.registry.getDomainConfiguration(ObjectMother.deprecatedDomain())).isNull();
	}

	@Test
	public void readReturnNullWhenDomainDoesNotExist() {
		assertThat(this.registry.getDomainConfiguration(ObjectMother.notExistingDomain())).isNull();
	}

	@Test
	public void readReturnsTheConfigurationWhenDomainExists() {
		assertThat(this.registry.getDomainConfiguration(ObjectMother.domain()).getDescription())
				.isEqualTo("Domain for testing Swiffer lib");
		assertThat(this.registry.getDomainConfiguration(ObjectMother.domain()).getRetention())
				.isEqualTo(Period.ofDays(1));
	}

}
