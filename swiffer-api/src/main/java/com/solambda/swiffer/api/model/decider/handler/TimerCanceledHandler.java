package com.solambda.swiffer.api.model.decider.handler;

import com.solambda.swiffer.api.model.decider.Decisions;
import com.solambda.swiffer.api.model.decider.context.TimerCanceledContext;

public interface TimerCanceledHandler extends EventContextHandler<TimerCanceledContext> {
	public void onTimerCanceled(TimerCanceledContext context, Decisions decisions);
}
