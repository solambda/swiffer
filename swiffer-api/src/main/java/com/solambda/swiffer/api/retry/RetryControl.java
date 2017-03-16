package com.solambda.swiffer.api.retry;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Control parameter for retry timer.
 *
 * @see RetryHandlers
 */
public class RetryControl implements Serializable{
    private static final long serialVersionUID = 824003101008069698L;

    /**
     * Prefix of retry timer ID.
     */
    public static final String RETRY_TIMER = "SWIFFER_RETRY_TIMER_FOR_";

    /**
     * Prefix of marker name for the number of retries.
     */
    static final String RETRY_MARKER = "SWIFFER_RETRY_MARKER_FOR_";

    private Long scheduledEventId;
    private String markerName;

    /**
     * Creates new control object for retry timer.
     *
     * @param scheduledEventId id of the ActivityTaskScheduled event that was recorded when activity task that failed was scheduled
     * @param activityName failed activity name
     */
    public RetryControl(Long scheduledEventId, String activityName) {
        this.scheduledEventId = Preconditions.checkNotNull(scheduledEventId);
        markerName = RETRY_MARKER + Preconditions.checkNotNull(activityName);
    }

    /**
     * Default non-argument constructor.
     */
    public RetryControl() {
    }

    public Long getScheduledEventId() {
        return scheduledEventId;
    }

    public void setScheduledEventId(Long scheduledEventId) {
        this.scheduledEventId = scheduledEventId;
    }

    public String getMarkerName() {
        return markerName;
    }

    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

    public static String getTimerId(String activityName) {
        return RETRY_TIMER + Preconditions.checkNotNull(activityName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RetryControl that = (RetryControl) o;
        return Objects.equal(scheduledEventId, that.scheduledEventId) &&
                Objects.equal(markerName, that.markerName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(scheduledEventId, markerName);
    }

    @Override
    public String toString() {
        return "RetryControl{" +
                "scheduledEventId=" + scheduledEventId +
                ", markerName='" + markerName + '\'' +
                '}';
    }
}
