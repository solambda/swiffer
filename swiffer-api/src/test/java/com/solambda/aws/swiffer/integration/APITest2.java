package com.solambda.aws.swiffer.integration;

import static org.mockito.Mockito.mock;

import java.util.function.*;

import com.solambda.aws.swiffer.api.model.tasks.TaskContext;

public class APITest2 {

	// public static final TaskDef<Integer, Double> TASK_2 =
	// Config.configure("name",
	// "version").input(Integer.class).output(Double.class);
	public static final TaskDef TASK_0 = null;
	public static final TaskDefIO<Integer, Double> TASK_1 = Config.configure(Integer.class, Double.class);
	public static final TaskDefI<Integer> TASK_2 = null;
	public static final TaskDefO<Double> TASK_3 = null;

	// registration / config time
	public static interface Config {

		public static TaskDef configure(String name, String version);

		@SuppressWarnings("unchecked")
		public static <I, O> TaskDefIO<I, O> configure(final Class<I> i, final Class<O> o) {
			return mock(TaskDefIO.class);
		}
		//
		// @SuppressWarnings("unchecked")
		// public static <I> TaskDefI<I> configure(final Class<I> i) {
		// return mock(TaskDefI.class);
		// }
		//
		// @SuppressWarnings("unchecked")
		// public static <O> TaskDefO<O> configure(final Class<O> o) {
		// return mock(TaskDefI.class);
		// }
	}

	public static interface TaskDef {
		String name();

		String version();

		<I> TaskDefI<I> input(Class<I> c);

		<O> TaskDefO<O> output(Class<O> c);
	}

	public static interface TaskDefIO<I, O> extends TaskDefI<I>, TaskDefO<O> {
	}

	public static interface TaskDefO<O> extends TaskDef {
		public Class<O> outputType();

		@Override
		<I> TaskDefIO<I, O> input(Class<I> c);

	}

	public static interface TaskDefI<I> extends TaskDef {
		public Class<I> inputType();

		@Override
		<O> TaskDefIO<I, O> output(Class<O> c);
	}

	public static interface TaskResultConfigurer<I, O> {
		public void completed(BiConsumer<TaskContext, DecisionMaker> code);
	}

	public static interface Serializer<I> {
		public String serialize(I input);
	}

	public static interface DecisionMaker {
		public void schedule(TaskDef d);

		public <I> void schedule(TaskDefI<I> d, I input);

		public <I> void schedule(TaskDefI<I> d, I input, Serializer<I> s);

		public <I> void schedule(TaskDefIO<I, ?> d, I input);

		public <I> void schedule(TaskDefIO<I, ?> d, I input, Serializer<I> s);

	}

	public static class DeciderDef {

		protected <I, O> TaskResultConfigurer<I, O> onTask(final TaskDefIO<I, O> d) {
			return null;
		}

		public void define() {
			onTask(TASK_1)
					.completed((c, d) -> {
						Integer input = 5;
						d.schedule(TASK_0);
						// d.schedule(TASK_0, input); wont compile
							d.schedule(TASK_1);// !! should not be allowed
							d.schedule(TASK_1, input);
							d.schedule(TASK_2);// !! should not be allowed
							d.schedule(TASK_2, input);
							d.schedule(TASK_3);
							// d.schedule(TASK_3, input); wont compile
						});

		}
	}

	public static interface TaskMapper<I, O> {
		public void to(Function<I, O> f);

		public void to(Consumer<I> f);

		public void to(Supplier<O> f);
	}

	public static interface TaskRegistry {
		public <I, O> TaskMapper<I, O> map(TaskDefIO<I, O> t);
	}

	public static class TaskExecutorDef {

		public void define(final TaskRegistry r) {
			r.map(TASK_1).to((i) -> i * 2.0);
		}
	}
}
