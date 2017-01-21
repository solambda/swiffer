package com.solambda.aws.swiffer.api.model.decider.handler;

import com.solambda.aws.swiffer.api.model.decider.Decisions;
import com.solambda.aws.swiffer.api.model.decider.context.MarkerRecordedContext;

public interface MarkerRecordedHandler extends EventContextHandler<MarkerRecordedContext> {

	void onMarkerRecorded(MarkerRecordedContext context, Decisions decision);
}
