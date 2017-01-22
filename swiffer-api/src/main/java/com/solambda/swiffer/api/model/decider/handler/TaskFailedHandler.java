package com.solambda.swiffer.api.model.decider.handler;

import com.solambda.swiffer.api.model.decider.Decisions;
import com.solambda.swiffer.api.model.decider.context.TaskFailedContext;

public interface TaskFailedHandler extends EventContextHandler<TaskFailedContext> {
	public void onTaskFailed(TaskFailedContext context, Decisions decisions);

}