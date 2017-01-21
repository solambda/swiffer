package com.solambda.swiffer.api.model.tasks;

public interface TaskExecutor {

	public void execute(TaskContext context, TaskReport reportTask);
}
