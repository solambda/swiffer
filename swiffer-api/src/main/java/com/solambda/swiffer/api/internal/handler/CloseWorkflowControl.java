package com.solambda.swiffer.api.internal.handler;

import java.io.Serializable;
import java.util.Objects;

import com.amazonaws.services.simpleworkflow.model.CancelWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.CompleteWorkflowExecutionDecisionAttributes;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.FailWorkflowExecutionDecisionAttributes;

/**
 * Control object which hold information about parameters for close workflow decisions.
 * This information is needed to reschedule close decision in case in has failed.
 * This object is recorded in specific marker and then fetched for retry
 * (since there is no way to retrieve this information from avaliable workflow history or failed event information).
 *
 * @see CloseWorkflowFailedHandlers
 */
public class CloseWorkflowControl implements Serializable {
    private static final long serialVersionUID = -32883342290518485L;

    public static final String CANCEL_MARKER = "SWIFFER_CANCEL_MARKER";
    public static final String COMPLETE_MARKER = "SWIFFER_COMPLETE_MARKER";
    public static final String FAIL_MARKER = "SWIFFER_FAIL_MARKER";

    private Object result;
    private String reason;
    private String details;

    private CloseWorkflowControl() {
    }

    /**
     * Creates control object with information for {@link DecisionType#CancelWorkflowExecution} decision.
     *
     * @param details details of the cancellation, optional
     * @return new control object
     * @see CancelWorkflowExecutionDecisionAttributes
     */
    public static CloseWorkflowControl cancelWorkflowControl(String details) {
        CloseWorkflowControl control = new CloseWorkflowControl();
        control.setDetails(details);

        return control;
    }

    /**
     * Creates control object with information for {@link DecisionType#FailWorkflowExecution} decision.
     *
     * @param reason  the reason for the failure
     * @param details details of the failure, optional
     * @return new control object
     * @see FailWorkflowExecutionDecisionAttributes
     */
    public static CloseWorkflowControl failWorkflowControl(String reason, String details) {
        CloseWorkflowControl control = new CloseWorkflowControl();
        control.setReason(reason);
        control.setDetails(details);

        return control;
    }

    /**
     * Creates control object with information for {@link DecisionType#CompleteWorkflowExecution} decision.
     *
     * @param result The result of the workflow execution
     * @return new control object
     * @see CompleteWorkflowExecutionDecisionAttributes
     */
    public static CloseWorkflowControl completeWorkflowControl(Object result) {
        CloseWorkflowControl control = new CloseWorkflowControl();
        control.setResult(result);

        return control;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "CloseWorkflowControl{" +
                "result=" + result +
                ", reason='" + reason + '\'' +
                ", details='" + details + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloseWorkflowControl that = (CloseWorkflowControl) o;
        return Objects.equals(result, that.result) &&
                Objects.equals(reason, that.reason) &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, reason, details);
    }
}
