package com.solambda.swiffer.api.model.tasks;

import com.solambda.swiffer.api.model.TaskType;

public interface TaskRegistry {

	public TaskInvoker get(TaskType type);
}
