package com.solambda.swiffer.api.model.tasks;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.solambda.swiffer.api.model.Failure;

public abstract class AbstractTaskExecutor implements TaskExecutor {

	@Override
	public void execute(final TaskContext context, final TaskReport reportTask) {
		try {
			String output = executeTask(context);
			reportTask.completed(output);
		} catch (Exception ex) {
			StringWriter errors = new StringWriter();
			ex.printStackTrace(new PrintWriter(errors));
			String details = errors.toString();
			reportTask.failed(Failure.reason(ex.getClass().getName()).details(details));
		}
	}

	protected abstract String executeTask(TaskContext context) throws Exception;

}
