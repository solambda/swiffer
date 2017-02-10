package com.solambda.swiffer.api;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
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
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.duration.DefaultDurationTransformer;
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.internal.registration.DomainRegistry;
import com.solambda.swiffer.api.internal.utils.SWFUtils;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.mapper.JacksonDataMapper;

public class Swiffer {
	private static final DataMapper DEFAULT_DATA_MAPPER = new JacksonDataMapper();

	private Logger LOGGER = LoggerFactory.getLogger(Swiffer.class);

	private AmazonSimpleWorkflow swf;
	private String domain;
	private final DataMapper dataMapper;
	private final DurationTransformer durationTransformer;

	/**
	 * Creates new Swiffer with default data mapper {@link JacksonDataMapper}.
	 */
	public Swiffer(final AmazonSimpleWorkflow swf, final String domain) {
		this(swf, domain, DEFAULT_DATA_MAPPER, new DefaultDurationTransformer());
	}

	/**
	 * Creates new Swiffer.
	 *
	 * @param swf interface for accessing Amazon SWF
	 * @param domain swf domain
	 * @param dataMapper custom {@link DataMapper} for serialization/deserialization of input and output
	 */
	public Swiffer(AmazonSimpleWorkflow swf, String domain, DataMapper dataMapper) {
		this(swf, domain, dataMapper, new DefaultDurationTransformer());
	}

	/**
	 * Creates new Swiffer.
	 * @param swf interface for accessing Amazon SWF
	 * @param domain swf domain
	 * @param dataMapper custom {@link DataMapper} for serialization/deserialization of input and output
	 * @param durationTransformer custom {@link DurationTransformer}
	 */
	public Swiffer(AmazonSimpleWorkflow swf, String domain, DataMapper dataMapper, DurationTransformer durationTransformer) {
		this.swf = Preconditions.checkNotNull(swf, "SWF client must be specified!");
		this.domain = Preconditions.checkNotNull(domain, "domain must be specified!");
		this.dataMapper = Preconditions.checkNotNull(dataMapper, "DataMapper must be specified");
		this.durationTransformer = Preconditions.checkNotNull(durationTransformer, "DurationTransformer must be specified");
	}

	public static Swiffer get(final AmazonSimpleWorkflow swf, final String domain) {
		return new Swiffer(swf, domain);
	}

	public WorkerBuilder newWorkerBuilder() {
		return new WorkerBuilder(this.swf, this.domain, this.dataMapper);
	}

	public DeciderBuilder newDeciderBuilder() {
		return new DeciderBuilder(this.swf, this.domain, this.dataMapper, this.durationTransformer);
	}

	/**
	 * Starts an execution of the workflow type in the specified domain using
	 * the provided workflowId and input data.
	 *
	 * @param workflowTypeDefinition
	 * @param workflowId
	 * @param tags
	 *            The list of tags to associate with the workflow execution. You
	 *            can specify a maximum of 5 tags. You can list workflow
	 *            executions with a specific tag by calling
	 *            ListOpenWorkflowExecutions or ListClosedWorkflowExecutions and
	 *            specifying a TagFilter.
	 * @return the runId of the execution
	 */
	public String startWorkflow(final Class<?> workflowTypeDefinition, final String workflowId, final Tags tags) {
		return startWorkflow(workflowTypeDefinition, workflowId, null, null, tags);
	}

	/**
	 * @param workflowTypeDefinition
	 * @param workflowId
	 * @param options
	 * @param tags
	 *            The list of tags to associate with the workflow execution. You
	 *            can specify a maximum of 5 tags. You can list workflow
	 *            executions with a specific tag by calling
	 *            ListOpenWorkflowExecutions or ListClosedWorkflowExecutions and
	 *            specifying a TagFilter.
	 * @return the runId of the execution
	 */
	public String startWorkflow(final Class<?> workflowTypeDefinition, final String workflowId,
			final WorkflowOptions options, final Tags tags) {
		return startWorkflow(workflowTypeDefinition, workflowId, null, options, tags);
	}

	/**
	 * @param workflowTypeDefinition
	 * @param workflowId
	 * @param input
	 *            the input for the workflow execution, to be made available to
	 *            the new workflow execution in the WorkflowExecutionStarted
	 *            history event.
	 * @return the runId of the execution
	 */
	public String startWorkflow(final Class<?> workflowTypeDefinition, final String workflowId, final Object input) {
		return startWorkflow(workflowTypeDefinition, workflowId, input, null, null);
	}

	/**
	 * @param workflowTypeDefinition
	 * @param workflowId
	 * @param input
	 *            the input for the workflow execution, to be made available to
	 *            the new workflow execution in the WorkflowExecutionStarted
	 *            history event.
	 * @param tags
	 *            the list of tags to associate with the workflow execution. You
	 *            can specify a maximum of 5 tags. You can list workflow
	 *            executions with a specific tag by calling
	 *            ListOpenWorkflowExecutions or ListClosedWorkflowExecutions and
	 *            specifying a TagFilter.
	 * @return the runId of the execution
	 */
	public String startWorkflow(final Class<?> workflowTypeDefinition, final String workflowId, final Object input,
			final Tags tags) {
		return startWorkflow(workflowTypeDefinition, workflowId, input, null, tags);
	}

	/**
	 * @param workflowTypeDefinition
	 * @param workflowId
	 * @param input
	 *            the input for the workflow execution, to be made available to
	 *            the new workflow execution in the WorkflowExecutionStarted
	 *            history event.
	 * @param options
	 * @param tags
	 *            The list of tags to associate with the workflow execution. You
	 *            can specify a maximum of 5 tags. You can list workflow
	 *            executions with a specific tag by calling
	 *            ListOpenWorkflowExecutions or ListClosedWorkflowExecutions and
	 *            specifying a TagFilter.
	 * @return the runId of the execution
	 */
	public String startWorkflow(final Class<?> workflowTypeDefinition, final String workflowId, final Object input,
			final WorkflowOptions options, final Tags tags) {
		return doStart(workflowTypeDefinition, workflowId, input, options, tags);
	}

	// CONVERSIONS

	private String serializeInput(final Object input) {
		return dataMapper.serialize(input);
	}

	private WorkflowType toSWFWorkflowType(final Class<?> workflowTypeDefinition) {

		final com.solambda.swiffer.api.WorkflowType annotation = workflowTypeDefinition
				.getAnnotation(com.solambda.swiffer.api.WorkflowType.class);
		return new WorkflowType().withName(annotation.name()).withVersion(annotation.version());
	}

	// SWF BRIDGE IMPL

	private String doStart(final Class<?> workflowTypeDefinition, final String workflowId, final Object input,
			final WorkflowOptions options, final Tags tags) {
		SWFUtils.checkId(workflowId);
		Preconditions.checkNotNull(workflowTypeDefinition, "a workflowTypeDefinition must be specified!");

		try {
			final WorkflowOptions opts = options == null ? new WorkflowOptions()
					: options;
			StartWorkflowExecutionRequest request = new StartWorkflowExecutionRequest()
					.withDomain(this.domain)
					.withWorkflowType(toSWFWorkflowType(workflowTypeDefinition))
					.withWorkflowId(workflowId)
					.withInput(serializeInput(input))
					.withTaskList(opts.getTaskList())
					.withTagList(tags == null ? Tags.none().get() : tags.get())
					.withExecutionStartToCloseTimeout(opts.getMaxExecutionDuration())
					.withTaskPriority(opts.getTaskPriority())
					.withTaskStartToCloseTimeout(opts.getMaxDecisionTaskDuration());
			if (opts.getChildTerminationPolicy() != null) {
				request = request.withChildPolicy(opts.getChildTerminationPolicy());
			}
			return this.swf.startWorkflowExecution(request)
					.getRunId();
		} catch (final WorkflowExecutionAlreadyStartedException e) {
			throw new IllegalStateException(
					String.format("Cannot start the workflow %s with id %s", workflowTypeDefinition, workflowId), e);
		}
	}

	/**
	 * Send a signal to the workflow represented by the given id.
	 *
	 * @param workflowId
	 *            the workflow id
	 * @param signalName
	 *            the signal name
	 *
	 * @throws IllegalStateException
	 *             if the workflow designated by this workflowId is not open
	 */
	public void sendSignalToWorkflow(final String workflowId, final String signalName) {

	}

	/**
	 * Send a signal to the workflow represented by the given id.
	 *
	 * @param workflowId
	 *            the workflow id
	 * @param signalName
	 *            the signal name
	 * @param input
	 *            the input to send with the signal
	 *
	 * @throws IllegalStateException
	 *             if the workflow designated by this workflowId is not open
	 */
	public void sendSignalToWorkflow(final String workflowId, final String signalName, final Object input) {
		doSignal(workflowId, signalName, input);
	}

	/**
	 * Return true if the workflow execution is open.
	 * <p>
	 *
	 * @param workflowId
	 *            the workflow id that was passed to
	 *            {@link #startWorkflow(Class, String, Object, WorkflowOptions, Tags)}
	 * @param runId
	 *            the runId of the execution, as returned by a call to
	 *            {@link #startWorkflow(Class, String, Object, WorkflowOptions, Tags)}
	 * @return true if the workflow is
	 */
	public boolean isWorkflowExecutionOpen(final String workflowId, final String runId) {
		try {
			final WorkflowExecutionInfo workflowExecution = getWorkflowExecution(workflowId, runId);
			return workflowExecution != null
					&& Objects.equals(workflowExecution.getExecutionStatus(), ExecutionStatus.OPEN.name());
		} catch (final UnknownResourceException e) {
			return false;
		}
	}

	public void terminateWorkflow(String workflowId, String runId, String reason){
		doTerminate(workflowId, runId, reason, null, null);
	}

	private WorkflowExecutionInfo getWorkflowExecution(final String workflowId, final String runId) {
		this.LOGGER.debug("[Domain: {}] Describe workflow {} execution {}", this.domain, workflowId, runId);
		final WorkflowExecutionDetail detail = this.swf.describeWorkflowExecution(new DescribeWorkflowExecutionRequest()
				.withDomain(this.domain)
				.withExecution(
						new WorkflowExecution()
								.withWorkflowId(workflowId)
								.withRunId(runId)));
		return detail.getExecutionInfo();
	}

	private void doSignal(final String workflowId, final String signalName, final Object input) {
		try {
			this.swf.signalWorkflowExecution(new SignalWorkflowExecutionRequest()
					.withDomain(this.domain)
					.withInput(serializeInput(input))
					.withSignalName(signalName)
					.withWorkflowId(workflowId));
		} catch (final UnknownResourceException e) {
			throw new IllegalStateException(
					String.format("Impossible to send signal '%s' to the workflow '%s'", signalName,
							workflowId),
					e);
		}
	}

	private void doTerminate(final String workflowId, final String runId, final String reason,
			final ChildPolicy childPolicy, final String details) {
		try {
			this.swf.terminateWorkflowExecution(new TerminateWorkflowExecutionRequest()
					.withDomain(this.domain)
					.withWorkflowId(workflowId)
					.withRunId(runId)
					.withChildPolicy(childPolicy != null ? childPolicy.toString() : null)
					.withReason(reason)
					.withDetails(details));
		} catch (final UnknownResourceException e) {
			throw new IllegalStateException(
					String.format("Impossible to terminate the workflow %s with runId %s", workflowId, runId), e);
		}
	}

	private List<WorkflowExecutionInfo> findClosedExecutions(final String workflowId) {
		final Date afterNow = new LocalDate().plusDays(1).toDateTimeAtStartOfDay().toDate();
		final Date ninetyDaysAgo = new LocalDate().minusDays(30).toDate();
		this.LOGGER.debug("Retrieving a closed execution of workflow id {} of domain {} between {} and {}",
				workflowId, this.domain, ninetyDaysAgo, afterNow);
		final WorkflowExecutionInfos infos = this.swf
				.listClosedWorkflowExecutions(new ListClosedWorkflowExecutionsRequest()
						.withDomain(this.domain)
						.withExecutionFilter(new WorkflowExecutionFilter().withWorkflowId(workflowId))
						.withStartTimeFilter(
								new ExecutionTimeFilter().withOldestDate(ninetyDaysAgo).withLatestDate(afterNow)));
		return infos.getExecutionInfos();
	}

	private List<WorkflowExecutionInfo> findOpenExecutions(final String workflowId) {
		final Date afterNow = new LocalDate().plusDays(1).toDateTimeAtStartOfDay().toDate();
		final Date ninetyDaysAgo = new LocalDate().minusDays(30).toDate();
		final WorkflowExecutionInfos infos = this.swf.listOpenWorkflowExecutions(new ListOpenWorkflowExecutionsRequest()
				.withDomain(this.domain)
				.withExecutionFilter(new WorkflowExecutionFilter().withWorkflowId(workflowId))
				.withStartTimeFilter(new ExecutionTimeFilter().withOldestDate(ninetyDaysAgo).withLatestDate(afterNow)));
		return infos.getExecutionInfos();
	}

	private void doCancel(final String workflowId, final String runId) {
		try {
			this.swf.requestCancelWorkflowExecution(new RequestCancelWorkflowExecutionRequest()
					.withDomain(this.domain)
					.withRunId(runId)
					.withWorkflowId(workflowId));
		} catch (final UnknownResourceException e) {
			throw new IllegalStateException(
					String.format("Impossible to cancel the workflow %s for the runId %s", workflowId, runId), e);
		}

	}

	/**
	 * @return true if swiffer can access AWS SWF, false otherwise
	 */
	public boolean checkConfiguration() {
		try {
			new DomainRegistry(this.swf, this.domain).listDomains();
			return true;
		} catch (final Exception e) {
			this.LOGGER.error("Swiffer cannot access AWS SWF", e);
			return false;
		}
	}

}
