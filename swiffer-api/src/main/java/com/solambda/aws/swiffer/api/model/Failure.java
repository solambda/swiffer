package com.solambda.aws.swiffer.api.model;

public class Failure {

	private String reason;
	private String details;

	public static Failure reason(final String reason) {
		return new Failure(reason);
	}

	public Failure(final String reason) {
		super();
		this.reason = reason;
	}

	public Failure details(final String details) {
		this.details = details;
		return this;
	}

	public String details() {
		return details;
	}

	public String reason() {
		return reason;
	}
}
