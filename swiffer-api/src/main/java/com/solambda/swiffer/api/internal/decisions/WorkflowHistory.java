package com.solambda.swiffer.api.internal.decisions;

import java.util.List;

import com.solambda.swiffer.api.EventHandlerCommonParameter;

@EventHandlerCommonParameter
public interface WorkflowHistory {

	/**
	 * Retrieve the complete list of the events of this workflow.
	 * <p>
	 *
	 * @return the {@link WorkflowEvent}s in the history of the workflow sorted
	 *         by descending {@link WorkflowEvent#id()}s (most-recent first)
	 */
	List<WorkflowEvent> events();

	/**
	 * Retrieve an event by its id
	 *
	 * @param eventId
	 * @return the event or null if the id does not match any event.
	 */
	WorkflowEvent getEventById(Long eventId);

}
