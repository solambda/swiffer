package com.solambda.swiffer.api.registries;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Period;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.solambda.swiffer.api.internal.domains.DomainConfiguration;
import com.solambda.swiffer.api.internal.registration.DomainRegistry;
import com.solambda.swiffer.test.Tests;

public class DomainRegistryTest {

	private DomainRegistry registry;

	@Before
	public void createRegistry() {
		this.registry = new DomainRegistry(Tests.swf(), Tests.DOMAIN);
	}

	@Test
	public void listDomain() {
		this.registry.listDomains();
	}

	@Test
	@Ignore
	public void createWorksAsExpected() throws Exception {
		// we wont do that, since domain are persisted for eternity...
		this.registry.register(
				new DomainConfiguration("Domain for testing Swiffer lib", Period.ofDays(1)));
	}

	@Test
	@Ignore
	public void existsReturnFalseWhenDomainIsDeprecated() {
		assertThat(this.registry.isRegistered()).isFalse();
	}

	@Test
	@Ignore
	public void existsReturnFalseWhenDomainDoesNotExist() {
		assertThat(this.registry.isRegistered()).isFalse();
	}

	@Test
	public void existsReturnTrueWhenDomainExist() {
		assertThat(this.registry.isRegistered()).isTrue();
	}

	@Test
	@Ignore
	public void readReturnNullWhenDomainIsDeprecated() {
		assertThat(this.registry.getDomainConfiguration()).isNull();
	}

	@Test
	@Ignore
	public void readReturnNullWhenDomainDoesNotExist() {
		assertThat(this.registry.getDomainConfiguration()).isNull();
	}

	@Test
	@Ignore
	public void readReturnsTheConfigurationWhenDomainExists() {
		assertThat(this.registry.getDomainConfiguration().getDescription())
				.isEqualTo("Domain for testing Swiffer lib");
		assertThat(this.registry.getDomainConfiguration().getRetention())
				.isEqualTo(Period.ofDays(1));
	}

}
