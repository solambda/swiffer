package com.solambda.aws.swiffer.api.model.decider.handler;

import com.solambda.aws.swiffer.api.model.decider.Decisions;
import com.solambda.aws.swiffer.api.model.decider.context.TaskCompletedContext;

public interface TaskCompletedHandler extends EventContextHandler<TaskCompletedContext> {

	public void onTaskCompleted(TaskCompletedContext context, Decisions d);
}
