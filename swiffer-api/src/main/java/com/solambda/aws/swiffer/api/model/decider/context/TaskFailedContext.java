package com.solambda.aws.swiffer.api.model.decider.context;

import com.solambda.aws.swiffer.api.model.HasDetails;
import com.solambda.aws.swiffer.api.model.HasReason;
import com.solambda.aws.swiffer.api.model.HasTaskType;

public interface TaskFailedContext extends EventContext, HasDetails, HasReason, HasTaskType {

}
