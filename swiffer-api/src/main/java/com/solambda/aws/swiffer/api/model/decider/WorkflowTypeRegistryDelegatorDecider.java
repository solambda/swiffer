package com.solambda.aws.swiffer.api.model.decider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.aws.swiffer.api.model.WorkflowTypeId;
import com.google.common.base.Preconditions;

/**
 * Delegate decision-making to the {@link Decider} associated to the workflow
 * type of the current {@link DecisionContext}.
 * <p>
 *
 */
public class WorkflowTypeRegistryDelegatorDecider implements Decider {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowTypeRegistryDelegatorDecider.class);

	private Map<WorkflowTypeId, EventDelegatorDecider> deciderMap;

	/**
	 * Create a new instance that delegate using the given list of
	 * {@link DeciderConfigurer}.
	 *
	 * @param configurers
	 */
	public WorkflowTypeRegistryDelegatorDecider(final List<DeciderConfigurer> configurers) {
		Preconditions.checkArgument(configurers != null, "configurers cannot be null");
		this.deciderMap = createMap(configurers);
	}

	private Map<WorkflowTypeId, EventDelegatorDecider> createMap(final List<DeciderConfigurer> configurers) {
		return configurers.stream()
				.collect(Collectors.toMap(
						c -> c.workflowType(),
						c -> new EventDelegatorDecider(c.registry())
						)
				);
	}

	@Override
	public void makeDecisions(final DecisionContext context, final Decisions decideTo) {
		EventDelegatorDecider decider = deciderMap.get(context.workflowType());
		if (decider == null) {
			LOGGER.error("impossible to find a decider for workflow {}: failing the workflow", context.workflowType(), context);
			decideTo.failWorfklow(String.format("no decider defined for workflow {}", context.workflowType()));
		} else {
			LOGGER.debug("decider for workflow {} is starting making decisions for {}", context.workflowType(), context);
			decider.makeDecisions(context, decideTo);
		}
	}
}
