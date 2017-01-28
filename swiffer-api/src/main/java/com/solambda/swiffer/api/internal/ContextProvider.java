package com.solambda.swiffer.api.internal;

public interface ContextProvider<T> {

	public abstract T get();

	public abstract void stop();

}