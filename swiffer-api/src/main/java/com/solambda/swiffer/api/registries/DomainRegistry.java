package com.solambda.swiffer.api.registries;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.*;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.model.DomainConfiguration;
import com.solambda.swiffer.api.model.DomainIdentifier;
import com.solambda.swiffer.api.utils.PeriodUtils;

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
		Preconditions.checkArgument(domainConfiguration.getRetention() == null || domainConfiguration.getRetention().getDays() <= 90,
				"Retention must be lower than or equals to 90 days");
		try {
			RegisterDomainRequest request = new RegisterDomainRequest()
					.withName(id.getName())
					.withDescription(domainConfiguration.getDescription())
					.withWorkflowExecutionRetentionPeriodInDays(
							domainConfiguration.getRetention() == null ? "NONE" : Integer.toString(domainConfiguration.getRetention().getDays()));
			client.registerDomain(request);
		} catch (DomainAlreadyExistsException e) {
			LOGGER.info("Fail to create domain {} : it already exists.", id.getName());
		} catch (Exception e) {
			throw new IllegalStateException("Cannot create domain " + id, e);
		}
	}

	public boolean domainExists(final DomainIdentifier id) {
		try {
			DomainDetail domainDetail = client.describeDomain(new DescribeDomainRequest().withName(id.getName()));
			DomainInfo domainInfo = domainDetail.getDomainInfo();
			return domainDetail != null && Objects.equals(domainInfo.getStatus(), "REGISTERED") ? true : false;
		} catch (UnknownResourceException e) {
			return false;
		}
	}

	/**
	 * @param id
	 *            the id of the domain to remove
	 */
	public void removeDomain(final DomainIdentifier id) {
		try {
			client.deprecateDomain(new DeprecateDomainRequest().withName(id.getName()));
		} catch (UnknownResourceException e) {
		} catch (DomainDeprecatedException e) {
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
			DomainDetail domainDetail = client.describeDomain(new DescribeDomainRequest().withName(id.getName()));
			DomainInfo domainInfo = domainDetail.getDomainInfo();
			return domainDetail != null && Objects.equals(domainInfo.getStatus(), "REGISTERED") ? new DomainConfiguration(domainInfo.getDescription(),
					PeriodUtils.toDays(domainDetail.getConfiguration().getWorkflowExecutionRetentionPeriodInDays()))
					: null;
		} catch (UnknownResourceException e) {
			return null;
		} catch (Exception e) {
			throw new IllegalStateException("cannot get the domain configuration for " + id, e);
		}
	}

}
