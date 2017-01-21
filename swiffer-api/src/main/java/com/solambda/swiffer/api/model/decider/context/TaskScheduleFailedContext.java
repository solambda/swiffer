package com.solambda.swiffer.api.model.decider.context;

import com.solambda.swiffer.api.model.HasCause;
import com.solambda.swiffer.api.model.HasTaskType;

public interface TaskScheduleFailedContext extends EventContext, HasCause, HasTaskType {

	public String taskId();

}
