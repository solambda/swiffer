package com.solambda.aws.swiffer.api.model.decider.handler;

import com.solambda.aws.swiffer.api.model.decider.Decisions;
import com.solambda.aws.swiffer.api.model.decider.context.TaskTimedOutContext;

public interface TaskTimedOutHandler extends EventContextHandler<TaskTimedOutContext> {
	public void onTaskTimedOut(TaskTimedOutContext context, Decisions decisions);
}
