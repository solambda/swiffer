package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.decisions.EventContext;
import com.solambda.swiffer.api.internal.events.HasCause;
import com.solambda.swiffer.api.internal.events.HasTaskType;

public interface ActivityTaskScheduleFailedContext extends EventContext, HasCause, HasTaskType {

	public String activityId();

}
