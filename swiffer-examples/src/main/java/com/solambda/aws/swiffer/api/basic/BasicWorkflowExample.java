package com.solambda.aws.swiffer.api.basic;

import java.time.Duration;
import java.time.LocalDateTime;

import com.solambda.aws.swiffer.api.model.*;
import com.solambda.aws.swiffer.api.model.decider.DeciderConfigurerImpl;
import com.solambda.aws.swiffer.api.model.decider.DeciderService;
import com.solambda.aws.swiffer.api.model.decider.context.EventContext;
import com.solambda.aws.swiffer.api.model.decider.context.identifier.MarkerName;
import com.solambda.aws.swiffer.api.model.decider.context.identifier.SignalName;
import com.solambda.aws.swiffer.utils.Constants;

public class BasicWorkflowExample {

	public static final TaskType SLEEP_TASK = new TaskType("sleep", "1.0");
	private DeciderService deciderService;
	private Workflow workflow;

	public static void main(final String[] args) {
		BasicWorkflowExample example = new BasicWorkflowExample();
		example.startTheWorkflowExternally();
		example.launchTaskExecutor();
		example.launchDecider();
		example.gracefullyStop();
	}

	private void gracefullyStop() {
		workflow.awaitClose();
		deciderService.stop();
		// deciderService.awaitStopped();
	}

	public static class DeciderExample extends DeciderConfigurerImpl {

		public DeciderExample() {
			super(Constants.WORKFLOW);
		}

		@Override
		protected void configure() {
			onWorkflow().started((c, d) -> d.scheduleTask(SLEEP_TASK, "5"));
			on(SLEEP_TASK).completed((c, d) -> d.completeWorfklow());
			on(SignalName.of("salut signal")).received((c, d) -> d.completeWorfklow("yo"));
			on(MarkerName.of("maker")).recorded((c, d) -> d.completeWorfklow("after marekr"));
			on(MarkerName.of("yo")).recorded((c, d) -> {

			});
		}

		protected T getState(final EventContext c) {
			String markerValue = findLatestStateMarker(c);
			T object = deserialize(markerValue);
			// T o = getState()
			T o = getState(c);

		}

		private String findLatestStateMarker(final EventContext c) {
			return null;
		}

	}

	private void launchDecider() {
		deciderService = Swiffer.get(Constants.swf(), Constants.DOMAIN)
				.decider(new DeciderExample())
				.start();
	}

	private void launchTaskExecutor() {
		Swiffer swiffer = Swiffer.get(Constants.swf(), Constants.DOMAIN);
		swiffer.task(SLEEP_TASK, this::sleep);
	}

	public void sleep(final String input) {
		sleep(Long.parseLong(input));
	}

	public void sleep(final long seconds) {
		try {
			Thread.sleep(seconds);
		} catch (InterruptedException e) {
		}
	}

	public void sleep(final Duration duration) {
		sleep(duration.getSeconds());
	}

	public String now() {
		return LocalDateTime.now().toString();
	}

	public long currentTimeMills() {
		return System.currentTimeMillis();
	}

	public String upperCase(final String input) {
		return input != null ? input.toUpperCase() : null;
	}

	private void startTheWorkflowExternally() {
		workflow = new WorkflowBuilder()
				.domain(Constants.DOMAIN)
				.client(Constants.swf())
				.type(Constants.WORKFLOW)
				.id("my-business-id")
				.build();
		workflow.start();
	}
}
