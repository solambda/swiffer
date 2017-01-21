package com.solambda.aws.swiffer.api.model.decider.context;

import com.solambda.aws.swiffer.api.model.HasInput;

public interface SignalReceivedContext extends EventContext, HasInput {

	public String signalName();

}
