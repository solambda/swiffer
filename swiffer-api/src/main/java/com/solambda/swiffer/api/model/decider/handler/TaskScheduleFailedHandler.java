package com.solambda.swiffer.api.model.decider.handler;

import com.solambda.swiffer.api.model.decider.Decisions;
import com.solambda.swiffer.api.model.decider.context.TaskScheduleFailedContext;

public interface TaskScheduleFailedHandler extends EventContextHandler<TaskScheduleFailedContext> {
	public void onTaskScheduleFailed(TaskScheduleFailedContext context, Decisions decisions);

}
