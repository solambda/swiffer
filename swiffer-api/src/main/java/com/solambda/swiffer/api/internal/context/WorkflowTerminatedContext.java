package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.decisions.EventContext;
import com.solambda.swiffer.api.internal.events.HasCause;
import com.solambda.swiffer.api.internal.events.HasDetails;
import com.solambda.swiffer.api.internal.events.HasReason;

public interface WorkflowTerminatedContext extends EventContext, HasReason, HasDetails, HasCause {

}
