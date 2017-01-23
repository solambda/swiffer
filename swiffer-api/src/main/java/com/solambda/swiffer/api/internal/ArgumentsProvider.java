package com.solambda.swiffer.api.internal;

import com.solambda.swiffer.api.internal.activities.ActivityTaskContext;

public interface ArgumentsProvider {

	public Object[] getArguments(ActivityTaskContext context);
}
