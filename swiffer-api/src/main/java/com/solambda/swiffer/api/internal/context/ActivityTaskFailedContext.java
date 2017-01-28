package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.decisions.EventContext;
import com.solambda.swiffer.api.internal.events.HasDetails;
import com.solambda.swiffer.api.internal.events.HasReason;
import com.solambda.swiffer.api.internal.events.HasTaskType;

public interface ActivityTaskFailedContext extends EventContext, HasDetails, HasReason, HasTaskType {

}
