package com.solambda.swiffer.api.internal.decisions;

import java.util.List;
import java.util.Optional;

import com.solambda.swiffer.api.internal.TaskContext;
import com.solambda.swiffer.api.internal.VersionedName;

/**
 * Provide information useful during decision making of a workflow.
 * <p>
 *
 *
 */
public interface DecisionTaskContext extends TaskContext {

	/**
	 * @return the workflow type id, used to identify this workflow
	 */
	VersionedName workflowType();

	/**
	 * @return the ID of workflow which is being executed
	 */
	String workflowId();

	/**
	 * @return the new {@link WorkflowEvent}s received since the last
	 *         decision-making, sorted by ascending {@link WorkflowEvent#id()}s
	 *         (most recent last)
	 */
	List<WorkflowEvent> newEvents();

    /**
     * Checks whether Marker with specified name was recoded.
     * Use this method to assess presence of the Marker without details.
     *
     * @param markerName Marker name
     * @return {@code true} if Marker with name {@code markerName} was recorded
     */
    boolean hasMarker(String markerName);

    /**
     * Returns details for the latest recorded Marker with specified name.
     * If there is no Marker with specified name of if the Marker was recorded without any details then {@link Optional#empty()} is returned.
     *
     * @param markerName Marker name
     * @param type       expected Marker details type
     * @param <T>        Marker details type
     * @return {@link Optional} with Marker details
     * @see #hasMarker(String) how to assess presence of the Marker withut details
     */
    <T> Optional<T> getMarkerDetails(String markerName, Class<T> type);
}
