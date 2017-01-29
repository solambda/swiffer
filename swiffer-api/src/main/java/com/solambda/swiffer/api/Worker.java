package com.solambda.swiffer.api;

/**
 * A {@link Worker} is a daemon service that polls activity tasks, execute the
 * activity tasks and respond to SWF with activity task completed or failed.
 * <p>
 * The service can be started and stopped using the methods {@link #start()} and
 * {@link #stop()}.
 * <p>
 * You create instances of {@link Worker}s using
 * {@link Swiffer#newWorkerBuilder()}
 *
 */
public interface Worker extends TaskListService {

}
