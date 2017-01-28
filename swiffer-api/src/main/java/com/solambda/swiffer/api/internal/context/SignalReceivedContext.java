package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.HasInput;
import com.solambda.swiffer.api.internal.decisions.EventContext;

public interface SignalReceivedContext extends EventContext, HasInput {

	public String signalName();

}
