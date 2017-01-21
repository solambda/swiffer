package com.solambda.aws.swiffer.api.model;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.*;

public class WorkflowImpl implements Workflow {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowImpl.class);
	protected AmazonSimpleWorkflow swf;
	protected String domain;
	private String name;
	private String version;
	private String workflowId;
	private String runId;

	public WorkflowImpl(final AmazonSimpleWorkflow swf, final String domain, final String name, final String version, final String workflowId) {
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
	public String start(final Options options) {
		return start(null, options);
	}

	@Override
	public String start(final String input, final Options options) {
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
		WorkflowExecutionInfo info = getWorkflowExecution();
		if (info == null) {
			throw new IllegalStateException(
					String.format("Impossible to check the close status of this workflow: it is not a particular execution of the workflow %s", workflowId));
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
		return runId != null;
	}

	@Override
	public void awaitClose() {
		if (isExecution()) {
			while (!isClosed()) {
				try {
					Thread.sleep(3000L);
				} catch (InterruptedException e) {
					throw new IllegalStateException(String.format(
							"Impossible to wait for the workflow %s to close", e));
				}
			}
		} else {
			throw new IllegalStateException(String.format(
					"Impossible to wait for the workflow %s to close: this instance is not a particular execution",
					workflowId));
		}
	}

	// UTILS

	private boolean hasOpenExecution() {
		try {
			WorkflowExecutionInfo workflowExecution = getWorkflowExecution();
			if (workflowExecution != null) {
				return Objects.equals(ExecutionStatus.OPEN.name(), getWorkflowExecution().getExecutionStatus());
			} else {
				return findOpenExecutions().size() > 0;
			}
		} catch (UnknownResourceException e) {
			return false;
		}
	}

	private boolean getExecutionCloseStatusEquals(final CloseStatus expectedStatus) {
		WorkflowExecutionInfo info = getWorkflowExecution();
		if (info == null) {
			throw new IllegalStateException(
					String.format("Impossible to get the execution status of this workflow: it is not a particular execution of the workflow %s", workflowId));
		}
		return Objects.equals(info.getExecutionStatus(), ExecutionStatus.CLOSED.name()) && Objects.equals(info.getCloseStatus(), expectedStatus.name());
	}

	// SWF BRIDGE

	private void doSignal(final String signalName, final String input) {
		try {
			swf.signalWorkflowExecution(new SignalWorkflowExecutionRequest()
					.withDomain(domain)
					.withInput(input)
					.withSignalName(signalName)
					.withWorkflowId(workflowId)
					.withRunId(runId));
		} catch (UnknownResourceException e) {
			throw new IllegalStateException(String.format("Impossible to send signal '%s' the workflow '%s' with runId '%s'", signalName, workflowId, runId), e);
		}
	}

	private void doTerminate(final String reason, final ChildPolicy childPolicy, final String details) {
		try {
			swf.terminateWorkflowExecution(new TerminateWorkflowExecutionRequest()
					.withDomain(domain)
					.withWorkflowId(workflowId)
					.withRunId(runId)
					.withChildPolicy(childPolicy != null ? childPolicy.toString() : null)
					.withReason(reason)
					.withDetails(details));
		} catch (UnknownResourceException e) {
			throw new IllegalStateException(String.format("Impossible to terminate the workflow %s with runId %s", workflowId, runId), e);
		}
	}

	private WorkflowExecutionInfo getWorkflowExecution() {
		if (runId == null) {
			return null;
		}
		LOGGER.debug("[Domain: {}] Describe workflow {} execution {}", domain, workflowId, runId);
		WorkflowExecutionDetail detail = swf.describeWorkflowExecution(new DescribeWorkflowExecutionRequest()
				.withDomain(domain)
				.withExecution(
						new WorkflowExecution()
								.withWorkflowId(workflowId)
								.withRunId(runId)));
		return detail.getExecutionInfo();
	}

	private List<WorkflowExecutionInfo> findClosedExecutions() {
		Date afterNow = new LocalDate().plusDays(1).toDateTimeAtStartOfDay().toDate();
		Date ninetyDaysAgo = new LocalDate().minusDays(30).toDate();
		LOGGER.debug("Retrieving a closed execution of workflow id {} of domain {} between {} and {}",
				workflowId, domain, ninetyDaysAgo, afterNow);
		WorkflowExecutionInfos infos = swf.listClosedWorkflowExecutions(new ListClosedWorkflowExecutionsRequest()
				.withDomain(domain)
				.withExecutionFilter(new WorkflowExecutionFilter().withWorkflowId(workflowId))
				.withStartTimeFilter(new ExecutionTimeFilter().withOldestDate(ninetyDaysAgo).withLatestDate(afterNow)));
		return infos.getExecutionInfos();
	}

	private List<WorkflowExecutionInfo> findOpenExecutions() {
		Date afterNow = new LocalDate().plusDays(1).toDateTimeAtStartOfDay().toDate();
		Date ninetyDaysAgo = new LocalDate().minusDays(30).toDate();
		WorkflowExecutionInfos infos = swf.listOpenWorkflowExecutions(new ListOpenWorkflowExecutionsRequest()
				.withDomain(domain)
				.withExecutionFilter(new WorkflowExecutionFilter().withWorkflowId(workflowId))
				.withStartTimeFilter(new ExecutionTimeFilter().withOldestDate(ninetyDaysAgo).withLatestDate(afterNow)));
		return infos.getExecutionInfos();
	}

	private String doStart(final String input, final Options options, final Tags tags) {
		if (runId != null) {
			throw new IllegalStateException(String.format("Impossible to start the workflow %s because it is already started as %s", workflowId, runId));
		}
		try {
			Options opts = options == null ? Options.maxWorkflowDuration(Duration.ofSeconds(30)) : options;
			return this.runId = swf.startWorkflowExecution(new StartWorkflowExecutionRequest()
					.withDomain(domain)
					.withInput(input)
					.withWorkflowType(new WorkflowType().withName(name).withVersion(version))
					.withWorkflowId(workflowId)
					.withTaskList(opts.getTaskList())
					.withTagList(tags != null ? tags.get() : null)
					.withExecutionStartToCloseTimeout(opts.getMaxExecutionDuration())
					.withChildPolicy(opts.getChildTerminationPolicy())
					.withTaskPriority(opts.getTaskPriority())
					.withTaskStartToCloseTimeout(opts.getMaxDecisionTaskDuration())
					)
					.getRunId();
		} catch (WorkflowExecutionAlreadyStartedException e) {
			throw new IllegalStateException("cannot start the workflow " + workflowId, e);
		}
	}

	private void doCancel() {
		try {
			swf.requestCancelWorkflowExecution(new RequestCancelWorkflowExecutionRequest()
					.withDomain(domain)
					.withRunId(runId)
					.withWorkflowId(workflowId)
					);
		} catch (UnknownResourceException e) {
			throw new IllegalStateException(String.format("Impossible to cancel the workflow %s for the runId %s", workflowId, runId), e);
		}

	}

}
