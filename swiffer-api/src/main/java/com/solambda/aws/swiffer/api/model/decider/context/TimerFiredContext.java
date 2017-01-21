package com.solambda.aws.swiffer.api.model.decider.context;

import com.solambda.aws.swiffer.api.model.HasControl;
import com.solambda.aws.swiffer.api.model.HasTimerId;

public interface TimerFiredContext extends EventContext, HasTimerId, HasControl {

}
