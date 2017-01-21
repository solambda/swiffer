package com.solambda.aws.swiffer.api.model.tasks;

import com.solambda.aws.swiffer.api.model.TaskType;

public interface TaskRegistry {

	public TaskInvoker get(TaskType type);
}
