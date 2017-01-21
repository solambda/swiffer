package com.solambda.aws.swiffer.api.model.decider.context;

import com.solambda.aws.swiffer.api.model.HasCause;
import com.solambda.aws.swiffer.api.model.HasDetails;
import com.solambda.aws.swiffer.api.model.HasReason;

public interface WorkflowTerminatedContext extends EventContext, HasReason, HasDetails, HasCause {

}
