package com.solambda.aws.swiffer.api.model.decider;

import com.solambda.aws.swiffer.api.model.TaskType;
import com.solambda.aws.swiffer.api.model.WorkflowTypeId;
import com.solambda.aws.swiffer.api.model.decider.context.*;
import com.solambda.aws.swiffer.api.model.decider.context.identifier.*;
import com.solambda.aws.swiffer.api.model.decider.handler.*;

public class EventContextHandlerRegistryBuilder {

	private EventContextHandlerRegistry registry;
	private WorkflowTypeId workflowTypeId;

	public EventContextHandlerRegistryBuilder(final WorkflowTypeId workflowTypeId) {
		super();
		this.workflowTypeId = workflowTypeId;
		this.registry = new EventContextHandlerRegistry();
	}

	public EventContextHandlerRegistry build() {
		return registry;
	}

	public TaskHandlerRegistryBuilder on(final TaskType type) {
		return new TaskHandlerRegistryBuilder(type);
	}

	public WorkflowExecutionHandlerRegistryBuilder onWorkflow() {
		return new WorkflowExecutionHandlerRegistryBuilder();
	}

	public SignalHandlerRegistryBuilder on(final SignalName signalName) {
		return new SignalHandlerRegistryBuilder(signalName);
	}

	public TimerHandlerRegistryBuilder on(final TimerName timerName) {
		return new TimerHandlerRegistryBuilder(timerName);
	}

	public MarkerHandlerRegistryBuilder on(final MarkerName markerName) {
		return new MarkerHandlerRegistryBuilder(markerName);
	}

	public final class TimerHandlerRegistryBuilder {
		private TimerName name;

		public TimerHandlerRegistryBuilder(final TimerName name) {
			super();
			this.name = name;
		}

		public EventContextHandlerRegistryBuilder fired(final TimerFiredHandler handler) {
			registry.register(name, TimerFiredContext.class, handler);
			return EventContextHandlerRegistryBuilder.this;
		}

		public EventContextHandlerRegistryBuilder canceled(final TimerCanceledHandler handler) {
			registry.register(name, TimerCanceledContext.class, handler);
			return EventContextHandlerRegistryBuilder.this;
		}

	}

	public final class MarkerHandlerRegistryBuilder {
		private MarkerName name;

		public MarkerHandlerRegistryBuilder(final MarkerName name) {
			super();
			this.name = name;
		}

		public EventContextHandlerRegistryBuilder recorded(final MarkerRecordedHandler handler) {
			registry.register(name, MarkerRecordedContext.class, handler);
			return EventContextHandlerRegistryBuilder.this;
		}
	}

	public final class SignalHandlerRegistryBuilder {
		private SignalName name;

		public SignalHandlerRegistryBuilder(final SignalName name) {
			super();
			this.name = name;
		}

		public EventContextHandlerRegistryBuilder received(final SignalReceivedHandler handler) {
			registry.register(name, SignalReceivedContext.class, handler);
			return EventContextHandlerRegistryBuilder.this;
		}
	}

	public final class WorkflowExecutionHandlerRegistryBuilder extends EventContextHandlerRegistryBuilder {
		private WorkflowName name;

		public WorkflowExecutionHandlerRegistryBuilder() {
			super(workflowTypeId);
			this.name = new WorkflowName(workflowTypeId);
		}

		public WorkflowExecutionHandlerRegistryBuilder started(final WorkflowStartedHandler handler) {
			registry.register(name, WorkflowStartedContext.class, handler);
			return this;
		}

		public WorkflowExecutionHandlerRegistryBuilder terminated(final WorkflowTerminatedHandler handler) {
			registry.register(name, WorkflowTerminatedContext.class, handler);
			return this;
		}

		public WorkflowExecutionHandlerRegistryBuilder cancelRequested(final WorkflowCancelRequestedHandler handler) {
			registry.register(name, WorkflowCancelRequestedContext.class, handler);
			return this;
		}

	}

	public final class TaskHandlerRegistryBuilder extends EventContextHandlerRegistryBuilder {
		private TaskName name;

		private TaskHandlerRegistryBuilder(final TaskType type) {
			super(workflowTypeId);
			this.name = new TaskName(type);
		}

		public TaskHandlerRegistryBuilder completed(final TaskCompletedHandler handler) {
			registry.register(name, TaskCompletedContext.class, handler);
			return this;
		}

		public TaskHandlerRegistryBuilder failed(final TaskFailedHandler handler) {
			registry.register(name, TaskFailedContext.class, handler);
			return this;
		}

		public TaskHandlerRegistryBuilder timedOut(final TaskTimedOutHandler handler) {
			registry.register(name, TaskTimedOutContext.class, handler);
			return this;
		}

		public TaskHandlerRegistryBuilder scheduleFailed(final TaskScheduleFailedHandler handler) {
			registry.register(name, TaskScheduleFailedContext.class, handler);
			return this;
		}
	}

}
