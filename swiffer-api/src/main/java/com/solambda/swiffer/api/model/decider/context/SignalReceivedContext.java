package com.solambda.swiffer.api.model.decider.context;

import com.solambda.swiffer.api.model.HasInput;

public interface SignalReceivedContext extends EventContext, HasInput {

	public String signalName();

}
