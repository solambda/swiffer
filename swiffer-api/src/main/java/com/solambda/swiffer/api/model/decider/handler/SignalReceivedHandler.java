package com.solambda.swiffer.api.model.decider.handler;

import com.solambda.swiffer.api.model.decider.Decisions;
import com.solambda.swiffer.api.model.decider.context.SignalReceivedContext;

public interface SignalReceivedHandler extends EventContextHandler<SignalReceivedContext> {
	public void onSignalReceived(SignalReceivedContext context, Decisions decisions);
}
