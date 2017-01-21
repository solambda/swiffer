package com.solambda.aws.swiffer.api.model;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;

public class AbstractSwfObject<Builder> {

	protected AmazonSimpleWorkflow swf;
	protected String domain;

	public AbstractSwfObject() {
		super();
	}

	@SuppressWarnings("unchecked")
	public Builder domain(final String domain) {
		this.domain = domain;
		return (Builder) this;
	}

	@SuppressWarnings("unchecked")
	public Builder client(final AmazonSimpleWorkflow swf) {
		this.swf = swf;
		return (Builder) this;
	}
}
