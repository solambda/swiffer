package com.solambda.swiffer.api.internal.context;

import com.solambda.swiffer.api.internal.decisions.EventContext;
import com.solambda.swiffer.api.internal.events.HasDetails;

public interface MarkerRecordedContext extends EventContext, HasDetails {
	public String markerName();
}
