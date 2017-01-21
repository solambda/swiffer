package com.solambda.swiffer.api.model.decider.context;

import com.solambda.swiffer.api.model.HasDetails;

public interface MarkerRecordedContext extends EventContext, HasDetails {
	public String markerName();
}
