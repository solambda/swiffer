package com.solambda.aws.swiffer.api.model;

import java.time.Period;

public class DomainConfiguration {

	private String description;
	private Period retention;

	public DomainConfiguration(final String description, final Period retention) {
		super();
		this.description = description;
		this.retention = retention;
	}

	public String getDescription() {
		return description;
	}

	public Period getRetention() {
		return retention;
	}

	@Override
	public String toString() {
		return "DomainConfiguration [description=" + description + ", retention=" + retention + "]";
	}

}
