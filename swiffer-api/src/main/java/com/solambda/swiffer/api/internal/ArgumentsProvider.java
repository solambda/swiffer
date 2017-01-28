package com.solambda.swiffer.api.internal;

public interface ArgumentsProvider<T> {

	/**
	 * Transform the given context into an array of arguments.
	 * 
	 * @param context
	 * @return
	 */
	public Object[] getArguments(T context);
}
