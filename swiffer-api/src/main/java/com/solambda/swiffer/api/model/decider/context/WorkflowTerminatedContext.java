package com.solambda.swiffer.api.model.decider.context;

import com.solambda.swiffer.api.model.HasCause;
import com.solambda.swiffer.api.model.HasDetails;
import com.solambda.swiffer.api.model.HasReason;

public interface WorkflowTerminatedContext extends EventContext, HasReason, HasDetails, HasCause {

}
