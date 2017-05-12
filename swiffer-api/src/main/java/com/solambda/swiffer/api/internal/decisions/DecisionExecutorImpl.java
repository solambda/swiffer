package com.solambda.swiffer.api.internal.decisions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.Decision;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.RespondDecisionTaskCompletedRequest;
import com.solambda.swiffer.api.Decisions;

public class DecisionExecutorImpl implements DecisionExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DecisionExecutorImpl.class);

	/**
	 * List of decisions that close the workflow.
	 */
	private static final List<String> CLOSE_DECISIONS = Arrays.asList(DecisionType.CancelWorkflowExecution.name(),
																	  DecisionType.CompleteWorkflowExecution.name(),
																	  DecisionType.FailWorkflowExecution.name(),
																	  DecisionType.ContinueAsNewWorkflowExecution.name());
	/**
	 * Evaluates if specified decision is the close workflow decision.
	 */
	private static final Predicate<Decision> isCloseDecision = d -> CLOSE_DECISIONS.contains(d.getDecisionType());

	/**
	 * List of decisions that could be added with close the workflow decision.
	 */
	private static final List<String> COMPATIBLE_WITH_CLOSE = Arrays.asList(DecisionType.CancelTimer.name(),
																			DecisionType.RecordMarker.name(),
																			DecisionType.StartChildWorkflowExecution.name(),
																			DecisionType.RequestCancelExternalWorkflowExecution.name());

	private AmazonSimpleWorkflow swf;

	public DecisionExecutorImpl(final AmazonSimpleWorkflow swf) {
		super();
		this.swf = swf;
	}

	@Override
	public void apply(final DecisionTaskContext context, final Decisions decisions) {
		LOGGER.debug("Receive {} decisions: {}", ((DecisionsImpl) decisions).get().size(), decisions);

		final Collection<Decision> decisionList = normalize(((DecisionsImpl) decisions).get());
		LOGGER.debug("Responding SWF with {} decisions: {}", decisionList.size(), decisionList);

		this.swf.respondDecisionTaskCompleted(new RespondDecisionTaskCompletedRequest()
													  .withDecisions(decisionList)
													  // FIXME: why and how to get it ? (appart from externally ?)
													  // .withExecutionContext(executionContext)
													  .withTaskToken(context.taskToken()));
	}

	/**
	 * Filter list of received decisions leaving only ones that could be processed without errors.
	 * Decisions could be removed from the list only if one of the close decisions is present:
	 * <ul>
	 * <li>remove <b>all</b> decisions <b>after</b> first close decision,</li>
	 * <li>remove decisions incompatible with close.</li>
	 * </ul>
	 *
	 * @param decisions list of decisions received after task processing
	 * @return list of decisions that could be executed without validation errors
	 * @see #CLOSE_DECISIONS
	 * @see #COMPATIBLE_WITH_CLOSE
	 */
	private List<Decision> normalize(List<Decision> decisions) {
		if (decisions.stream().anyMatch(isCloseDecision)) {
			List<Decision> normalized = new ArrayList<>();
			boolean alreadyClosed = false;
			for (Decision decision : decisions) {
				String decisionType = decision.getDecisionType();
				if (alreadyClosed) {
					LOGGER.warn("Close must be last decision in list, skip decision {}.", decision);
				} else {
					if (CLOSE_DECISIONS.contains(decisionType)) {
						normalized.add(decision);
						alreadyClosed = true;
					} else {
						if (COMPATIBLE_WITH_CLOSE.contains(decisionType)) {
							normalized.add(decision);
						} else {
							LOGGER.warn("Close decision is incompatible with this decision, skip decision {}.", decision);
						}
					}
				}
			}

			return normalized;
		}

		return new ArrayList<>(decisions);
	}
}
