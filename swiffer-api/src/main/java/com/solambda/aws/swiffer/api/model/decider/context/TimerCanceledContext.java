package com.solambda.aws.swiffer.api.model.decider.context;

import com.solambda.aws.swiffer.api.model.HasControl;
import com.solambda.aws.swiffer.api.model.HasTimerId;

public interface TimerCanceledContext extends EventContext, HasTimerId, HasControl {

}
