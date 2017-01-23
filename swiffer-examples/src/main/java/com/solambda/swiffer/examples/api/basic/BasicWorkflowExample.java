package com.solambda.swiffer.examples.api.basic;

import java.time.Duration;
import java.time.LocalDateTime;

import com.solambda.swiffer.api.Swiffer;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.model.Workflow;
import com.solambda.swiffer.api.model.WorkflowBuilder;
import com.solambda.swiffer.api.model.WorkflowTypeId;
import com.solambda.swiffer.api.model.decider.DeciderConfigurerImpl;
import com.solambda.swiffer.api.model.decider.DeciderService;
import com.solambda.swiffer.api.model.decider.context.EventContext;
import com.solambda.swiffer.api.model.decider.context.identifier.MarkerName;
import com.solambda.swiffer.api.model.decider.context.identifier.SignalName;
import com.solambda.swiffer.examples.utils.Constants;

public class BasicWorkflowExample {

	public static final VersionedName SLEEP_TASK = new VersionedName("sleep", "1.0");
	private DeciderService deciderService;
	private Workflow workflow;

	public static void main(final String[] args) {
		final BasicWorkflowExample example = new BasicWorkflowExample();
		example.startTheWorkflowExternally();
		example.launchTaskExecutor();
		example.launchDecider();
		example.gracefullyStop();
	}

	private void gracefullyStop() {
		this.workflow.awaitClose();
		this.deciderService.stop();
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

		// protected T getState(final EventContext c) {
		// final String markerValue = findLatestStateMarker(c);
		// final T object = deserialize(markerValue);
		// // T o = getState()
		// final T o = getState(c);
		//
		// }

		private String findLatestStateMarker(final EventContext c) {
			return null;
		}

		@Override
		public WorkflowTypeId workflowType() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	private void launchDecider() {
		// deciderService = Swiffer.get(Constants.swf(), Constants.DOMAIN)
		// .decider(new DeciderExample())
		// .start();
	}

	private void launchTaskExecutor() {
		final Swiffer swiffer = Swiffer.get(Constants.swf(), Constants.DOMAIN);
		// swiffer.task(SLEEP_TASK, this::sleep);
	}

	public void sleep(final String input) {
		sleep(Long.parseLong(input));
	}

	public void sleep(final long seconds) {
		try {
			Thread.sleep(seconds);
		} catch (final InterruptedException e) {
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
		this.workflow = new WorkflowBuilder()
				.domain(Constants.DOMAIN)
				.client(Constants.swf())
				.type(Constants.WORKFLOW)
				.id("my-business-id")
				.build();
		this.workflow.start();
	}
}
