package com.solambda.swiffer.api.internal.registration;

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
import com.amazonaws.services.simpleworkflow.model.RegisterDomainRequest;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.internal.domains.DomainConfiguration;
import com.solambda.swiffer.api.internal.domains.DomainIdentifier;
import com.solambda.swiffer.api.internal.utils.PeriodUtils;

/**
 * Provide methods for registering or not a new domain in AWS.
 *
 */
public class DomainRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(DomainRegistry.class);

	private AmazonSimpleWorkflow client;

	public DomainRegistry(final AmazonSimpleWorkflow client) {
		super();
		this.client = client;
	}

	public void createDomain(final DomainIdentifier id, final DomainConfiguration domainConfiguration) {
		Preconditions.checkArgument(
				domainConfiguration.getRetention() == null || domainConfiguration.getRetention().getDays() <= 90,
				"Retention must be lower than or equals to 90 days");
		try {
			final RegisterDomainRequest request = new RegisterDomainRequest()
					.withName(id.getName())
					.withDescription(domainConfiguration.getDescription())
					.withWorkflowExecutionRetentionPeriodInDays(
							domainConfiguration.getRetention() == null ? "NONE"
									: Integer.toString(domainConfiguration.getRetention().getDays()));
			this.client.registerDomain(request);
		} catch (final DomainAlreadyExistsException e) {
			LOGGER.info("Fail to create domain {} : it already exists.", id.getName());
		} catch (final Exception e) {
			throw new IllegalStateException("Cannot create domain " + id, e);
		}
	}

	public boolean domainExists(final DomainIdentifier id) {
		try {
			final DomainDetail domainDetail = this.client
					.describeDomain(new DescribeDomainRequest().withName(id.getName()));
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
	public void removeDomain(final DomainIdentifier id) {
		try {
			this.client.deprecateDomain(new DeprecateDomainRequest().withName(id.getName()));
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
	public DomainConfiguration getDomainConfiguration(final DomainIdentifier id) {
		try {
			final DomainDetail domainDetail = this.client
					.describeDomain(new DescribeDomainRequest().withName(id.getName()));
			final DomainInfo domainInfo = domainDetail.getDomainInfo();
			return domainDetail != null && Objects.equals(domainInfo.getStatus(), "REGISTERED")
					? new DomainConfiguration(domainInfo.getDescription(),
							PeriodUtils.toDays(
									domainDetail.getConfiguration().getWorkflowExecutionRetentionPeriodInDays()))
					: null;
		} catch (final UnknownResourceException e) {
			return null;
		} catch (final Exception e) {
			throw new IllegalStateException("cannot get the domain configuration for " + id, e);
		}
	}

}
