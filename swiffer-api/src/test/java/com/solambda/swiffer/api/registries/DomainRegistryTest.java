package com.solambda.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Period;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.model.DomainConfiguration;
import com.solambda.swiffer.api.model.DomainIdentifier;
import com.solambda.swiffer.api.registries.DomainRegistry;
import com.solambda.swiffer.api.test.ObjectMother;

public class DomainRegistryTest {

	private DomainRegistry registry;

	@Before
	public void createRegistry() {
		AmazonSimpleWorkflow client = ObjectMother.client();
		this.registry = new DomainRegistry(client);
	}

	@Test
	public void createWorksAsExpected() throws Exception {
		// we wont do that, since domain are persisted for eternity...
		registry.createDomain(new DomainIdentifier(ObjectMother.domainName()), new DomainConfiguration("Domain for testing Swiffer lib", Period.ofDays(1)));
	}

	@Test
	public void existsReturnFalseWhenDomainIsDeprecated() {
		assertThat(registry.domainExists(ObjectMother.deprecatedDomain())).isFalse();
	}

	@Test
	public void existsReturnFalseWhenDomainDoesNotExist() {
		assertThat(registry.domainExists(ObjectMother.notExistingDomain())).isFalse();
	}

	@Test
	public void existsReturnTrueWhenDomainExist() {
		assertThat(registry.domainExists(ObjectMother.domain())).isTrue();
	}

	@Test
	public void readReturnNullWhenDomainIsDeprecated() {
		assertThat(registry.getDomainConfiguration(ObjectMother.deprecatedDomain())).isNull();
	}

	@Test
	public void readReturnNullWhenDomainDoesNotExist() {
		assertThat(registry.getDomainConfiguration(ObjectMother.notExistingDomain())).isNull();
	}

	@Test
	public void readReturnsTheConfigurationWhenDomainExists() {
		assertThat(registry.getDomainConfiguration(ObjectMother.domain()).getDescription()).isEqualTo("Domain for testing Swiffer lib");
		assertThat(registry.getDomainConfiguration(ObjectMother.domain()).getRetention()).isEqualTo(Period.ofDays(1));
	}

}
