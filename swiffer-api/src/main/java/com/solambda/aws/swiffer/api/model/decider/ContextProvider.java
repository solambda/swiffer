package com.solambda.aws.swiffer.api.model.decider;

public interface ContextProvider<T> {

	public abstract T get();

	public abstract void stop();

}