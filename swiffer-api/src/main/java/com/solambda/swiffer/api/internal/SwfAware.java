package com.solambda.swiffer.api.internal;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;

public interface SwfAware {

	public AmazonSimpleWorkflow swf();

	public String domain();
}
