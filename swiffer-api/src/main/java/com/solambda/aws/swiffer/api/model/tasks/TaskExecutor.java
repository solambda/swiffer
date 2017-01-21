package com.solambda.aws.swiffer.api.model.tasks;

public interface TaskExecutor {

	public void execute(TaskContext context, TaskReport reportTask);
}
