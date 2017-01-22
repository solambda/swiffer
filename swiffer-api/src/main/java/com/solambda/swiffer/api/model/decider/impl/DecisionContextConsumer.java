package com.solambda.swiffer.api.model.decider.impl;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.swiffer.api.internal.DecisionsImpl;
import com.solambda.swiffer.api.model.decider.*;

public class DecisionContextConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DecisionContextConsumer.class);

	private ContextProvider<DecisionContext> provider;
	private Decider decider;
	private DecisionExecutor executor;

	public DecisionContextConsumer(
			final AmazonSimpleWorkflow swf,
			final String domain,
			final String taskList,
			final String identity,
			final ExecutorService service,
			final Decider decider) {
		this(swf, domain, taskList, identity, service, decider, null);
	}

	public DecisionContextConsumer(
			final AmazonSimpleWorkflow swf,
			final String domain,
			final String taskList,
			final String identity,
			final ExecutorService service,
			final Decider decider,
			final Duration timeout) {
		super();
		DecisionContextProviderImpl dcpi = new DecisionContextProviderImpl(swf, domain, taskList, identity);
		this.provider = new ContextProviderThreadedImpl<DecisionContext>(dcpi, service, timeout);
		this.decider = decider;
		this.executor = new DecisionExecutorImpl(swf);
	}

	public void consumeOfFail() {
		consume(true);
	}

	public void stop() {
		LOGGER.debug("stopping consumer");
		provider.stop();
	}

	public void consume() {
		consume(false);
	}

	private void consume(final boolean failIfNoContext) {
		LOGGER.debug("Consuming next decision context");
		DecisionContext context = provider.get();
		if (context != null) {
			try {
				LOGGER.debug("Received a new decision context to process");
				DecisionsImpl decisions = new DecisionsImpl();
				decider.makeDecisions(context, decisions);
				executor.applyDecisions(context, decisions);
			} catch (Exception e) {
				throw new IllegalStateException("error during workflow decisions", e);
			}
		} else if (failIfNoContext) {
			LOGGER.debug("Failing because no decision context");
			throw new IllegalStateException("No context to consume !");
		} else {
			LOGGER.debug("no decision context available");
		}
	}
}
