package com.solambda.swiffer.api.model.decider.impl;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.Service.State;
import com.solambda.swiffer.api.model.decider.Decider;
import com.solambda.swiffer.api.model.decider.DeciderService;

public class DeciderServiceImpl implements DeciderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeciderServiceImpl.class);

	private DecisionContextConsumer consumer;

	private AbstractScheduledService delegate = null;

	public DeciderServiceImpl(final AmazonSimpleWorkflow swf, final String domain, final String taskList, final String identity,
			final int maxConcurrentDecisions, final Decider decider) {
		this(swf, domain, taskList, identity, maxConcurrentDecisions, null, decider);
	}

	public DeciderServiceImpl(final AmazonSimpleWorkflow swf, final String domain, final String taskList, final String identity,
			final int maxConcurrentDecisions, final Duration timeout, final Decider decider) {
		super();
		ExecutorService service = Executors.newFixedThreadPool(maxConcurrentDecisions);
		this.consumer = new DecisionContextConsumer(swf, domain, taskList, identity, service, decider, timeout);
	}

	@Override
	public void start() {
		LOGGER.debug("starting service");
		if (delegate == null) {
			init();
		} else {
			if (delegate.state() == State.RUNNING || delegate.state() == State.STARTING) {
				throw new IllegalStateException("Cannot start a service that is already running");
			}
			if (delegate.state() == State.STOPPING) {
				delegate.awaitTerminated();
			}
			if (delegate.state() == State.FAILED || delegate.state() == State.TERMINATED) {
				init();
			}
		}
		delegate.startAsync();
	}

	private void init() {
		delegate = new AbstractScheduledService() {
			@Override
			protected void runOneIteration() throws Exception {
				try {
					LOGGER.debug("running next iteration");
					consumer.consume();
				} catch (Exception e) {
					throw new IllegalStateException("Service problem", e);
				}
			}

			@Override
			protected Scheduler scheduler() {
				return Scheduler.newFixedDelaySchedule(0, 1, TimeUnit.SECONDS);
			}

			@Override
			protected void shutDown() throws Exception {
				LOGGER.debug("shutDown hook");
				super.shutDown();
				consumer.stop();
			}

		};
	}

	@Override
	public void stop() {
		if (delegate != null && (delegate.state() == State.RUNNING || delegate.state() == State.STARTING)) {
			LOGGER.debug("stopping service async");
			delegate.stopAsync();
			consumer.stop();
		}
	}

	@Override
	public void awaitStopped() {
		try {
			if (delegate != null) {
				delegate.awaitTerminated();
			}
		} catch (Exception e) {
			LOGGER.error("cannot wait for terminated", e);
		}
	}
}
