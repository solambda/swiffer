package com.solambda.aws.swiffer.api.model.decider.context;

import com.solambda.aws.swiffer.api.model.HasDetails;

public interface MarkerRecordedContext extends EventContext, HasDetails {
	public String markerName();
}
