package com.solambda.swiffer.api;

/**
 * A {@link Decider} polls decision tasks, and use workflow templates to make
 * decisions to SWF. The poller can be started and stopped using the methods
 * {@link #start()} and {@link #stop()}.
 * <p>
 * You create instances of {@link Decider}s using
 * {@link Swiffer#newDeciderBuilder()}
 *
 */
public interface Decider extends TaskListPoller {

}
