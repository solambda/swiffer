package com.solambda.aws.swiffer.api.model.decider.context;

import com.solambda.aws.swiffer.api.model.HasCause;
import com.solambda.aws.swiffer.api.model.HasTaskType;

public interface TaskScheduleFailedContext extends EventContext, HasCause, HasTaskType {

	public String taskId();

}
