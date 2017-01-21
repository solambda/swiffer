package com.solambda.swiffer.api.model.decider.context;

import com.solambda.swiffer.api.model.HasControl;
import com.solambda.swiffer.api.model.HasTimerId;

public interface TimerFiredContext extends EventContext, HasTimerId, HasControl {

}
