package com.solambda.swiffer.api.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvoker {

	private Object object;
	private Method method;

	public MethodInvoker(final Object object, final Method method) {
		super();
		this.object = object;
		this.method = method;
	}

	public Object invoke(final Object... args) throws InvocationTargetException {
		try {
			return this.method.invoke(this.object, args);
		} catch (final IllegalAccessException e) {
			throw new IllegalStateException("should never occurs", e);
		} catch (final IllegalArgumentException e) {
			throw new IllegalStateException("should never occurs", e);
		} catch (final InvocationTargetException e) {
			// the method invoked thrown an exception =>
			throw e;
		}
	}
}
