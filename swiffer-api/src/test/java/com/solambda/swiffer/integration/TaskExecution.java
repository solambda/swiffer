package com.solambda.swiffer.integration;

/**
 * TODO: business exception
 * <ul>
 * <li>should fail the workflow, registering info for business diagnostic
 * <li>behavior can be customized by the user
 * </ul>
 * TODO: technical exception
 * <ul>
 * <li>should provide a good default retry strategy (3 times, with bouncing
 * delay)
 * <li>the retry strategy can be customized
 * <li>at the end of the retries, should fail the workflow
 * <li>can be handled by the workflow if the retry is not needed
 * </ul>
 * TODO: nominal
 * <ul>
 * <li>output must be registered
 * </ul>
 * TODO: state retrieval
 * <ul>
 * <li>current WF state can be retrieved
 * <li>initial state can be retrieved
 * <li>test paginated retrieval
 * <li>
 * </ul>
 * TODO: output
 * <ul>
 * <li>output marshalling can fail => dev error
 * <li>must be registered for the next task input
 * <li>task without output can be gracefully handled
 * </ul>
 *
 * TODO: input
 * <ul>
 * <li>input unmarshalling can fail => dev error / version checking...
 * <li>task may not be interested in any input
 * <li>input must be passed
 * </ul>
 *
 * TODO: timeouts
 * <ul>
 * <li>timeouts for tasks must be detected and correctly handled
 * <li>default behavior for timeouts
 * <li>custom behavior for timeouts
 * <li>
 * </ul>
 *
 * TODO: can a task can cancel itself ?
 * <ul>
 * <li>timeouts for tasks must be detected and correctly handled
 * <li>default behavior for timeouts
 * <li>custom behavior for timeouts
 * <li>
 * </ul>
 *
 * TODO: incremental / long-term tasks
 * <ul>
 * <li>should heartbeat automatically
 * <li>progress reporting should be gracefully part of the API
 * <li>cancel request must be transparent
 * </ul>
 *
 */
public class TaskExecution {

}
