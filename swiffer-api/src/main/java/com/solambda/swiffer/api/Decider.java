package com.solambda.swiffer.api;

/**
 * A {@link Decider} is a daemon service that polls decision tasks, and use
 * workflow templates to make decisions to SWF.
 * <p>
 * The service can be started and stopped using the methods {@link #start()} and
 * {@link #stop()}.
 * <p>
 * You create instances of {@link Decider}s using
 * {@link Swiffer#newDeciderBuilder()}
 *
 */
public interface Decider extends TaskListService {

}
