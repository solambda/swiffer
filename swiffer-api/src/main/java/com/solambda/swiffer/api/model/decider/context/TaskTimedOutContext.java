package com.solambda.swiffer.api.model.decider.context;

import com.solambda.swiffer.api.model.HasDetails;
import com.solambda.swiffer.api.model.HasReason;
import com.solambda.swiffer.api.model.HasTaskType;

public interface TaskTimedOutContext extends EventContext, HasDetails, HasReason, HasTaskType {

}