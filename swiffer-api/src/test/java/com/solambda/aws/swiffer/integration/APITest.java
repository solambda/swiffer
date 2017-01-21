package com.solambda.aws.swiffer.integration;

import java.util.Map;
import java.util.function.BiConsumer;

import org.junit.Test;

import com.solambda.aws.swiffer.api.model.TaskType;
import com.solambda.aws.swiffer.api.model.decider.Decisions;
import com.solambda.aws.swiffer.api.model.decider.context.TaskCompletedContext;

public class APITest {

	public void deciderRegister() {
		Decisions s = null;
		s.scheduleTask(new TaskType("", ""));
		Object o = new Object() {
			TaskType TASK_1 = new TaskType("name", "version");
			TaskType TASK_2 = new TaskType("", "");

			public void registerTaskComplete(final Map<TaskType, BiConsumer<TaskCompletedContext, Decisions>> map) {
				map.put(TASK_1, (c, d) -> {
					d.scheduleTask(TASK_2, c.output());
				});

				onTask(TASK_1)
						.completed((TaskCompletedContext c, Decisions d) -> {
					MyBean output = c.output(MyBean.class);
					d.scheduleTask(TASK_2);
				})
						.failed()
						.canceled()
						.timedOut();

				onTask(TASK_2).completed((TaskCompletedContext c, Decisions d) -> {

					d.completeWorkflow();
				});

				onSignal("signalName").received();

				onTimer("timer").fired().canceled();

			}
		};

	}

	@Test
	public void taskExecutor() {
		// 2 cases:
		// -the poller is in its own JVM/app => must know the mapping
		// ActivityType <=> Task
		// -the poller is part of a bigger app with a decider, registry, etc...

		// AUTOMATIC ONESHOT TASKS
		// we want to launch a poller with a "name"
		// that repeatedly polls a "task list" of a "domain" for tasks
		// and execute them
		// and return the result of the task execution when done
		// and report failures appropriately
		// TaskExecutor
		// // technical config
		// .credentials("Credentials")
		// .region("Region")
		// // contextual config
		// .domain("domain")
		// // name of the executor (for logging purpose, optional,
		// // generated on the fly)
		// .named("poller-1")
		// // means the poller will not poll another task before the
		// // previous task has finish execution
		// .synchronous()
		// // means the poller can poll another task while the previous
		// // task is executing (default)
		// .asynchronous()
		// .poll("taskList");

		// AUTOMATIC INCREMENTAL TASKS
		// we want to launch a poller with a "name"
		// that repeatedly polls a "task list" of a "domain" for tasks
		// and execute them incrementally,
		// having the opportunity for the decider to cancel the task
		// and mark the task as cancelled if needed
		// and return the result of the task execution when done

		// MANUAL TASKS
		// we want to launch a poller with a "name"
		// that repeatedly polls a "task list" of a "domain" for tasks
		// and push the "task token" and "task input" and "context" somewhere
		// in order for an human to execute the task
		// and when the human did the task (push a button, etc...)
		// then the result is returned to the system

		// looping

	}

	@Test
	public void deciderPoller() {
		// DecisionTaskPoller poller = DecisionTaskPoller.ofDomain("domain")
		// .identifiedBy("identity")
		// .credentials(credentialsOrProvider)
		// .client(client)
		// .polls("taskList");
		// .startPollingOnce()//default task list
		// .startPollingOnce("taskList")
		// .startPolling()
		// .startPolling("taskList")
		// ;
		// poller.stop();
		// poller.start();
		// TODO metrics

		// not used externally .nextPage("token")
		// sensible defaults .maxPageSize(123)
		// not used externally .reverseOrder()
		// not used externally .normalOrder();
	}
}
