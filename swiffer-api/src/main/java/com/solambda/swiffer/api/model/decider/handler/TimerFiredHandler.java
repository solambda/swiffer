package com.solambda.swiffer.api.model.decider.handler;

import com.solambda.swiffer.api.model.decider.Decisions;
import com.solambda.swiffer.api.model.decider.context.TimerFiredContext;

public interface TimerFiredHandler extends EventContextHandler<TimerFiredContext> {
	public void onTimerFired(TimerFiredContext context, Decisions decisions);
}
