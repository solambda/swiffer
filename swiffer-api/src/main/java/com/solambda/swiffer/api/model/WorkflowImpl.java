package com.solambda.swiffer.api.model;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.CloseStatus;
import com.amazonaws.services.simpleworkflow.model.DescribeWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.ExecutionStatus;
import com.amazonaws.services.simpleworkflow.model.ExecutionTimeFilter;
import com.amazonaws.services.simpleworkflow.model.ListClosedWorkflowExecutionsRequest;
import com.amazonaws.services.simpleworkflow.model.ListOpenWorkflowExecutionsRequest;
import com.amazonaws.services.simpleworkflow.model.RequestCancelWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.SignalWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.StartWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.TerminateWorkflowExecutionRequest;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecution;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionAlreadyStartedException;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionDetail;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionFilter;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionInfo;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionInfos;
import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.solambda.swiffer.api.WorkflowOptions;

public class WorkflowImpl implements Workflow {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowImpl.class);
	protected AmazonSimpleWorkflow swf;
	protected String domain;
	private String name;
	private String version;
	private String workflowId;
	private String runId;

	public WorkflowImpl(final AmazonSimpleWorkflow swf, final String domain, final String name, final String version,
			final String workflowId) {
		super();
		this.swf = swf;
		this.domain = domain;
		this.name = name;
		this.version = version;
		this.workflowId = workflowId;
	}

	// LIFECYCLE CHANGE METHODS

	@Override
	public String start() {
		return start(null, null);
	}

	@Override
	public String start(final String input) {
		return start(input, null);
	}

	@Override
	public String start(final WorkflowOptions options) {
		return start(null, options);
	}

	@Override
	public String start(final String input, final WorkflowOptions options) {
		return doStart(input, options, null);
	}

	@Override
	public void requestCancel() {
		doCancel();
	}

	@Override
	public void terminate() {
		doTerminate(null, null, null);
	}

	@Override
	public void terminate(final ChildPolicy childTerminationPolicy) {
		doTerminate(null, childTerminationPolicy, null);
	}

	@Override
	public void terminate(final Failure failure, final ChildPolicy childTerminationPolicy) {
		doTerminate(failure.reason(), childTerminationPolicy, failure.details());
	}

	@Override
	public void terminate(final Failure failure) {
		doTerminate(failure.reason(), null, failure.details());
	}

	@Override
	public void signal(final String signalName) {
		signal(signalName, null);
	}

	@Override
	public void signal(final String signalName, final String input) {
		doSignal(signalName, input);
	}

	// STATUS VISIBILITY METHODS

	@Override
	public boolean isStarted() {
		return hasOpenExecution();
	}

	@Override
	public boolean isClosed() {
		final WorkflowExecutionInfo info = getWorkflowExecution();
		if (info == null) {
			throw new IllegalStateException(
					String.format(
							"Impossible to check the close status of this workflow: it is not a particular execution of the workflow %s",
							this.workflowId));
		}
		return Objects.equals(info.getExecutionStatus(), ExecutionStatus.CLOSED.name());
	}

	@Override
	public boolean isCanceled() {
		return getExecutionCloseStatusEquals(CloseStatus.CANCELED);
	}

	@Override
	public boolean isTerminated() {
		return getExecutionCloseStatusEquals(CloseStatus.TERMINATED);
	}

	@Override
	public boolean isTimedOut() {
		return getExecutionCloseStatusEquals(CloseStatus.TIMED_OUT);
	}

	@Override
	public boolean isComplete() {
		return getExecutionCloseStatusEquals(CloseStatus.COMPLETED);
	}

	@Override
	public boolean isContinued() {
		return getExecutionCloseStatusEquals(CloseStatus.CONTINUED_AS_NEW);
	}

	@Override
	public boolean isFailed() {
		return getExecutionCloseStatusEquals(CloseStatus.FAILED);
	}

	@Override
	public boolean isExecution() {
		return this.runId != null;
	}

	@Override
	public void awaitClose() {
		if (isExecution()) {
			while (!isClosed()) {
				try {
					Thread.sleep(3000L);
				} catch (final InterruptedException e) {
					throw new IllegalStateException(String.format(
							"Impossible to wait for the workflow %s to close", e));
				}
			}
		} else {
			throw new IllegalStateException(String.format(
					"Impossible to wait for the workflow %s to close: this instance is not a particular execution",
					this.workflowId));
		}
	}

	// UTILS

	private boolean hasOpenExecution() {
		try {
			final WorkflowExecutionInfo workflowExecution = getWorkflowExecution();
			if (workflowExecution != null) {
				return Objects.equals(ExecutionStatus.OPEN.name(), getWorkflowExecution().getExecutionStatus());
			} else {
				return findOpenExecutions().size() > 0;
			}
		} catch (final UnknownResourceException e) {
			return false;
		}
	}

	private boolean getExecutionCloseStatusEquals(final CloseStatus expectedStatus) {
		final WorkflowExecutionInfo info = getWorkflowExecution();
		if (info == null) {
			throw new IllegalStateException(
					String.format(
							"Impossible to get the execution status of this workflow: it is not a particular execution of the workflow %s",
							this.workflowId));
		}
		return Objects.equals(info.getExecutionStatus(), ExecutionStatus.CLOSED.name())
				&& Objects.equals(info.getCloseStatus(), expectedStatus.name());
	}

	// SWF BRIDGE

	private void doSignal(final String signalName, final String input) {
		try {
			this.swf.signalWorkflowExecution(new SignalWorkflowExecutionRequest()
					.withDomain(this.domain)
					.withInput(input)
					.withSignalName(signalName)
					.withWorkflowId(this.workflowId)
					.withRunId(this.runId));
		} catch (final UnknownResourceException e) {
			throw new IllegalStateException(
					String.format("Impossible to send signal '%s' the workflow '%s' with runId '%s'", signalName,
							this.workflowId, this.runId),
					e);
		}
	}

	private void doTerminate(final String reason, final ChildPolicy childPolicy, final String details) {
		try {
			this.swf.terminateWorkflowExecution(new TerminateWorkflowExecutionRequest()
					.withDomain(this.domain)
					.withWorkflowId(this.workflowId)
					.withRunId(this.runId)
					.withChildPolicy(childPolicy != null ? childPolicy.toString() : null)
					.withReason(reason)
					.withDetails(details));
		} catch (final UnknownResourceException e) {
			throw new IllegalStateException(
					String.format("Impossible to terminate the workflow %s with runId %s", this.workflowId, this.runId),
					e);
		}
	}

	private WorkflowExecutionInfo getWorkflowExecution() {
		if (this.runId == null) {
			return null;
		}
		LOGGER.debug("[Domain: {}] Describe workflow {} execution {}", this.domain, this.workflowId, this.runId);
		final WorkflowExecutionDetail detail = this.swf.describeWorkflowExecution(new DescribeWorkflowExecutionRequest()
				.withDomain(this.domain)
				.withExecution(
						new WorkflowExecution()
								.withWorkflowId(this.workflowId)
								.withRunId(this.runId)));
		return detail.getExecutionInfo();
	}

	private List<WorkflowExecutionInfo> findClosedExecutions() {
		final Date afterNow = new LocalDate().plusDays(1).toDateTimeAtStartOfDay().toDate();
		final Date ninetyDaysAgo = new LocalDate().minusDays(30).toDate();
		LOGGER.debug("Retrieving a closed execution of workflow id {} of domain {} between {} and {}",
				this.workflowId, this.domain, ninetyDaysAgo, afterNow);
		final WorkflowExecutionInfos infos = this.swf
				.listClosedWorkflowExecutions(new ListClosedWorkflowExecutionsRequest()
						.withDomain(this.domain)
						.withExecutionFilter(new WorkflowExecutionFilter().withWorkflowId(this.workflowId))
						.withStartTimeFilter(
								new ExecutionTimeFilter().withOldestDate(ninetyDaysAgo).withLatestDate(afterNow)));
		return infos.getExecutionInfos();
	}

	private List<WorkflowExecutionInfo> findOpenExecutions() {
		final Date afterNow = new LocalDate().plusDays(1).toDateTimeAtStartOfDay().toDate();
		final Date ninetyDaysAgo = new LocalDate().minusDays(30).toDate();
		final WorkflowExecutionInfos infos = this.swf.listOpenWorkflowExecutions(new ListOpenWorkflowExecutionsRequest()
				.withDomain(this.domain)
				.withExecutionFilter(new WorkflowExecutionFilter().withWorkflowId(this.workflowId))
				.withStartTimeFilter(new ExecutionTimeFilter().withOldestDate(ninetyDaysAgo).withLatestDate(afterNow)));
		return infos.getExecutionInfos();
	}

	private String doStart(final String input, final WorkflowOptions options, final Tags tags) {
		if (this.runId != null) {
			throw new IllegalStateException(
					String.format("Impossible to start the workflow %s because it is already started as %s",
							this.workflowId, this.runId));
		}
		try {
			final WorkflowOptions opts = options == null ? new WorkflowOptions()
					.maxWorkflowDuration(Duration.ofSeconds(30)) : options;
			return this.runId = this.swf.startWorkflowExecution(new StartWorkflowExecutionRequest()
					.withDomain(this.domain)
					.withInput(input)
					.withWorkflowType(new WorkflowType().withName(this.name).withVersion(this.version))
					.withWorkflowId(this.workflowId)
					.withTaskList(opts.getTaskList())
					.withTagList(tags != null ? tags.get() : null)
					.withExecutionStartToCloseTimeout(opts.getMaxExecutionDuration())
					.withChildPolicy(opts.getChildTerminationPolicy())
					.withTaskPriority(opts.getTaskPriority())
					.withTaskStartToCloseTimeout(opts.getMaxDecisionTaskDuration()))
					.getRunId();
		} catch (final WorkflowExecutionAlreadyStartedException e) {
			throw new IllegalStateException("cannot start the workflow " + this.workflowId, e);
		}
	}

	private void doCancel() {
		try {
			this.swf.requestCancelWorkflowExecution(new RequestCancelWorkflowExecutionRequest()
					.withDomain(this.domain)
					.withRunId(this.runId)
					.withWorkflowId(this.workflowId));
		} catch (final UnknownResourceException e) {
			throw new IllegalStateException(
					String.format("Impossible to cancel the workflow %s for the runId %s", this.workflowId, this.runId),
					e);
		}

	}

}
