package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.decisions.EventContext;
import com.solambda.swiffer.api.internal.events.HasOutput;
import com.solambda.swiffer.api.internal.events.HasTaskType;

public interface ActivityTaskCompletedContext extends EventContext, HasOutput, HasTaskType {

}
