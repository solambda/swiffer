package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.decisions.EventContext;
import com.solambda.swiffer.api.internal.events.HasControl;
import com.solambda.swiffer.api.internal.events.HasTimerId;

public interface TimerCanceledContext extends EventContext, HasTimerId, HasControl {

}
