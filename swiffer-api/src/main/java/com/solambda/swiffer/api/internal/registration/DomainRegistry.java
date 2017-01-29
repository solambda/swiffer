package com.solambda.swiffer.api.internal.registration;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DeprecateDomainRequest;
import com.amazonaws.services.simpleworkflow.model.DescribeDomainRequest;
import com.amazonaws.services.simpleworkflow.model.DomainAlreadyExistsException;
import com.amazonaws.services.simpleworkflow.model.DomainDeprecatedException;
import com.amazonaws.services.simpleworkflow.model.DomainDetail;
import com.amazonaws.services.simpleworkflow.model.DomainInfo;
import com.amazonaws.services.simpleworkflow.model.DomainInfos;
import com.amazonaws.services.simpleworkflow.model.ListDomainsRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterDomainRequest;
import com.amazonaws.services.simpleworkflow.model.RegistrationStatus;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.internal.domains.DomainConfiguration;
import com.solambda.swiffer.api.internal.utils.PeriodUtils;

/**
 * Provide methods for registering or not a new domain in AWS.
 *
 */
public class DomainRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainRegistry.class);

	private AmazonSimpleWorkflow client;
	private String domain;

	public DomainRegistry(final AmazonSimpleWorkflow client, final String domain) {
		super();
		this.client = client;
		this.domain = domain;
	}

	public void listDomains() {
		final DomainInfos domainInfos = this.client.listDomains(new ListDomainsRequest()
				.withRegistrationStatus(RegistrationStatus.REGISTERED));
		final List<DomainInfo> domainInfos2 = domainInfos.getDomainInfos();
		for (final DomainInfo domainInfo : domainInfos2) {
			System.out.println(domainInfo);
		}
	}

	public void register(final DomainConfiguration domainConfiguration) {
		Preconditions.checkArgument(
				domainConfiguration.getRetention() == null || domainConfiguration.getRetention().getDays() <= 90,
				"Retention must be lower than or equals to 90 days");
		try {
			final RegisterDomainRequest request = new RegisterDomainRequest()
					.withName(this.domain)
					.withDescription(domainConfiguration.getDescription())
					.withWorkflowExecutionRetentionPeriodInDays(
							domainConfiguration.getRetention() == null ? "NONE"
									: Integer.toString(domainConfiguration.getRetention().getDays()));
			this.client.registerDomain(request);
		} catch (final DomainAlreadyExistsException e) {
			LOGGER.info("Fail to create domain {} : it already exists.", this.domain);
		} catch (final Exception e) {
			throw new IllegalStateException("Cannot create domain " + this.domain, e);
		}
	}

	public boolean isRegistered() {
		try {
			final DomainDetail domainDetail = this.client
					.describeDomain(new DescribeDomainRequest().withName(this.domain));
			final DomainInfo domainInfo = domainDetail.getDomainInfo();
			return domainDetail != null && Objects.equals(domainInfo.getStatus(), "REGISTERED") ? true : false;
		} catch (final UnknownResourceException e) {
			return false;
		}
	}

	/**
	 * @param id
	 *            the id of the domain to remove
	 */
	public void deprecate() {
		try {
			this.client.deprecateDomain(new DeprecateDomainRequest().withName(this.domain));
		} catch (final UnknownResourceException e) {
		} catch (final DomainDeprecatedException e) {
		}
	}

	/**
	 * @param id
	 * @return the domain, or null if the domain does not exist or if it is
	 *         deprecated
	 * @throws IllegalStateException
	 *             if if's impossible to get the domain configuration
	 */
	public DomainConfiguration getDomainConfiguration() {
		try {
			final DomainDetail domainDetail = this.client
					.describeDomain(new DescribeDomainRequest().withName(this.domain));
			final DomainInfo domainInfo = domainDetail.getDomainInfo();
			return domainDetail != null && Objects.equals(domainInfo.getStatus(), "REGISTERED")
					? new DomainConfiguration(domainInfo.getDescription(),
							PeriodUtils.toDays(
									domainDetail.getConfiguration().getWorkflowExecutionRetentionPeriodInDays()))
					: null;
		} catch (final UnknownResourceException e) {
			return null;
		} catch (final Exception e) {
			throw new IllegalStateException("cannot get the domain configuration for " + this.domain, e);
		}
	}

}
