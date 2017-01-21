package com.solambda.aws.swiffer.api.model.decider.impl;

import java.time.Duration;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solambda.aws.swiffer.api.model.decider.ContextProvider;

public class ContextProviderThreadedImpl<T> implements ContextProvider<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContextProviderThreadedImpl.class);

	private ExecutorService service;
	private Duration timeout;
	private ContextProvider<T> delegate;
	private CompletableFuture<T> context;

	public ContextProviderThreadedImpl(final ContextProvider<T> delegate, final ExecutorService service) {
		this(delegate, service, null);
	}

	public ContextProviderThreadedImpl(final ContextProvider<T> delegate, final ExecutorService service, final Duration timeout) {
		this.delegate = delegate;
		this.service = service;
		this.timeout = timeout;
	}

	@Override
	public T get() {
		context = CompletableFuture.supplyAsync(() -> delegate.get(), service);
		try {
			if (timeout != null) {
				return context.get(timeout.getSeconds(), TimeUnit.SECONDS);
			} else {
				return context.get();
			}
		} catch (CancellationException e) {
			return null;
		} catch (TimeoutException e1) {
			return null;
		} catch (Exception e2) {
			throw new IllegalStateException("Cannot poll a decision task", e2);
		}
	}

	@Override
	public void stop() {
		LOGGER.debug("cancelling future");
		context.complete(null);
		context.cancel(true);
		service.shutdown();
	}
}
