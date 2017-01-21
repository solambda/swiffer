package com.solambda.aws.swiffer.api.model.tasks;


public class RegistryBasedTaskExecutor extends AbstractTaskExecutor {

	private TaskRegistry registry;

	public RegistryBasedTaskExecutor(final TaskRegistry registry) {
		super();
		this.registry = registry;
	}

	@Override
	protected String executeTask(final TaskContext context) throws Exception {
		TaskInvoker invoker = registry.get(context.taskType());
		return invoker.invoke(context.input());
	}

}
