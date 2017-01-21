package com.solambda.swiffer.api.test;

import static org.assertj.core.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.swiffer.api.model.decider.*;
import com.solambda.swiffer.api.model.decider.impl.DecisionExecutorImpl;
import com.solambda.swiffer.api.model.decider.impl.DecisionsImpl;

public class WorkflowConsumerTest {

	private static Logger LOGGER = LoggerFactory.getLogger(WorkflowConsumerTest.class);
	private final ExecutorService executor = Executors.newFixedThreadPool(10);

	private ContextProvider<DecisionContext> contextProvider;
	private Decider decider;

	public WorkflowConsumerTest(final Decider decider, final ContextProvider<DecisionContext> provider) {
		super();
		this.contextProvider = provider;
		this.decider = decider;
	}

	public void consume() {
		// should eimt an asynchronous task, timeout to accelerate error cases

		DecisionContext context = getContextTimeout(Duration.ofSeconds(5));
		if (context != null) {
			try {
				DecisionsImpl decisions = new DecisionsImpl();
				decider.makeDecisions(context, decisions);
				DecisionExecutor applier = new DecisionExecutorImpl(ObjectMother.client());
				applier.applyDecisions(context, decisions);
			} catch (Exception e) {
				fail("error during workflow decisions", e);
			}
		} else {
			fail("impossible to consume decision task: there is not !");
		}
	}

	private DecisionContext getContextTimeout(final Duration ofSeconds) {
		Future<DecisionContext> context = executor.submit(() -> contextProvider.get());
		try {
			return context.get(ofSeconds.getSeconds(), TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			return null;
		} catch (Exception e) {
			throw new IllegalStateException("cannot poll for decision task", e);
		}
	}
}
