package com.solambda.swiffer.api.internal.decisions;

import com.amazonaws.services.simpleworkflow.model.DecisionTask;

/**
 * History fetch mode for {@link DecisionTaskPoller}.
 */
public enum HistoryMode {
    /**
     * No history is fetched with {@link DecisionTask}.
     */
    NONE,

    /**
     * All history is fetched with {@link DecisionTask}.
     */
    EAGER,

    /**
     * History is fetched on-demand with {@link DecisionTask}.
     */
    LAZY
}
