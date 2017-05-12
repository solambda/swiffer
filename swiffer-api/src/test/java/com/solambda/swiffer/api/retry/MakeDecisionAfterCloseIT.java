package com.solambda.swiffer.api.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.AmazonSimpleWorkflowException;
import com.amazonaws.services.simpleworkflow.model.DecisionType;
import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.*;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Test workflow behaviour for the cases when activity is retried after workflow is cancelled or completed.
 * This situation may result in two cases:
 * <ol>
 * <li>
 * Validation exception by Amazon SWF;
 * </li>
 * <li>
 * Decision to complete/cancel is not executed and failure event is registered.
 * </li>
 * </ol>
 * The exact steps leading to one or the other case are not clear (depends on timing of the scheduled event?).
 * <h1>Case 1</h1>
 * <p>
 * According to the Amazon Documentation:
 * <blockquote>
 * Amazon SWF checks to ensure that the decision to close or cancel the workflow execution is the last decision sent by the decider.
 * That is, it isn't valid to have a set of decisions in which there are decisions after the one that closes the workflow.
 * </blockquote>
 * In this case amazon throws a {@link AmazonSimpleWorkflowException} with {@link AmazonServiceException.ErrorType#Client}
 * and {@link AmazonServiceException#errorCode} "ValidationException" (note: the official documentation has wrong error code)
 * and {@link AmazonServiceException#errorMessage} = "Close must be last decision in list" (or "Specified decision is incompatible with close decision").
 * <h1>Case 2</h1>
 * The exception is not thrown and the Workflow is not closed. In this case there are events:
 * <ul>
 * <li>
 * For cancel: {@link EventType#CancelWorkflowExecutionFailed} with cause {@code UNHANDLED_DECISION}
 * </li>
 * <li>
 * For complete: {@link EventType#CompleteWorkflowExecutionFailed} with cause {@code UNHANDLED_DECISION}
 * </li>
 * </ul>
 * From the Amazon Documentation:
 * <blockquote>
 * An UnhandledDecision fault will be returned if a workflow closing decision is specified and a signal or activity
 * event had been added to the history while the decision task was being performed by the decider.
 * Unlike the above situations which are logic issues, this fault is always possible because of race conditions in a distributed system.
 * </blockquote>
 * <p>
 * The expected behaviour to prevent both cases:
 * <ul>
 * <li>Make sure that decisions are not added after decision to close workflow (TBD)</li>
 * <li>If the decision was added then decider shouldn't stop after the exceptions is received from Amazon SWF</li>
 * </ul>
 *
 * @see <a href="http://docs.aws.amazon.com/amazonswf/latest/developerguide/swf-dg-dev-deciders.html#swf-dg-closing-workflows">Closing a Workflow Execution</a>
 * @see <a href="https://docs.aws.amazon.com/AWSEC2/latest/APIReference/errors-overview.html#api-error-codes-table-client">Client Errors For Specific Actions</a>
 * @see <a href="https://docs.aws.amazon.com/amazonswf/latest/apireference/API_Decision.html">UnhandledDecision fault</a>
 */
@RunWith(JUnitParamsRunner.class)
public class MakeDecisionAfterCloseIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(MakeDecisionAfterCloseIT.class);

    private static final String TASK_LIST = "MakeDecisionAfterCloseIT-task-list";
    private static final String CLOSE_WF_SIGNAL = "MakeDecisionAfterCloseIT-close-wf-signal";
    private static final String CASE1_WF_SIGNAL = "MakeDecisionAfterCloseIT-case1-wf-signal";
    private static final String CANCEL_TIMER = "CANCEL_TIMER_MakeDecisionAfterCloseIT";

    private static final RetryPolicy GLOBAL_RETRY_POLICY = new ConstantTimeRetryPolicy(Duration.ofSeconds(1));

    @Retention(RetentionPolicy.RUNTIME)
    @WorkflowType(name = "MakeDecisionAfterCloseIT-workflow", version = "1", defaultTaskList = TASK_LIST)
    public static @interface RetryWorkflow{}

    @ActivityType(name = "MakeDecisionAfterCloseIT-failing-activity", version = "1", defaultTaskList = TASK_LIST)
    public static @interface FailingActivity {
    }

    @ActivityType(name = "MakeDecisionAfterCloseIT-default-activity", version = "1", defaultTaskList = TASK_LIST)
    public static @interface DefaultActivity {
    }

    @ActivityType(name = "MakeDecisionAfterCloseIT-cancel-activity", version = "1", defaultTaskList = TASK_LIST)
    public static @interface CancelActivity {
    }

    @RetryWorkflow
    public static class RetryWorkflowTemplate {

        @OnWorkflowStarted
        public void onStart(Decisions decideTo) {
            decideTo.scheduleActivityTask(FailingActivity.class, "");
        }

        @OnWorkflowCancelRequested
        public void onCancelRequested(String cause, Decisions decideTo) throws InterruptedException {
            LOGGER.info("Cancel request received.");
            decideTo.cancelWorkflow(cause);
            Thread.sleep(6 * 1000);
            LOGGER.info("Cancel workflow decision send.");
        }

        @OnSignalReceived(CLOSE_WF_SIGNAL)
        public void onCloseWFSignalReceived(@Input DecisionType decisionType, Decisions decideTo) throws InterruptedException {
            LOGGER.info("Signal received.");
            switch (decisionType) {
                case CompleteWorkflowExecution:
                    decideTo.completeWorkflow();
                    break;
                case FailWorkflowExecution:
                    decideTo.failWorkflow("Fail on signal", "Deliberate failure");
                    break;
                case CancelWorkflowExecution:
                    decideTo.cancelWorkflow("Cancel on signal");
                    break;
                case ContinueAsNewWorkflowExecution:
                    // TODO: revisit in Issue #11
                    decideTo.continueAsNewWorkflow("1");
                    break;
            }

            Thread.sleep(8 * 1000);
            LOGGER.info("{} decision send.", decisionType);
        }

        @OnSignalReceived(CASE1_WF_SIGNAL)
        public void onCase1SignalReceived(Decisions decideTo) throws InterruptedException {
            // this is intended to be mocked
        }

        @OnActivityCompleted(DefaultActivity.class)
        public void onDefaultActivity(Decisions decideTo) throws InterruptedException {

        }
    }

    public static class Executors {

        @Executor(activity = FailingActivity.class)
        public void failingActivity() throws InterruptedException {
            throw new RuntimeException("This activity is always failing, retried by default");
        }

        @Executor(activity = DefaultActivity.class)
        public void defaultActivity() throws InterruptedException {
            LOGGER.info("Default activity");
        }
    }

    private static final String DOMAIN = "github-swiffer";
    private static Swiffer swiffer;

    private static final Executors executor = spy(new Executors());
    private static final RetryWorkflowTemplate template = spy(new RetryWorkflowTemplate());
    private static Worker worker;
    private static Decider decider;

    private String runId;
    private String workflowId;

    @Test
    public void retryScheduledAfterCancel() throws Exception {
        workflowId = "retryScheduledAfterCancel";
        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.cancelWorkflow(workflowId, runId);

        sleep(Duration.ofSeconds(20));
        assertThat(decider.isStarted()).isTrue(); // case #1
        waitForWorkflowToClose(5); // case #2
    }

    private Object[] closeDecisions() {
        return new Object[]{
                new Object[]{"FailWorkflowExecution", DecisionType.FailWorkflowExecution},
                new Object[]{"CancelWorkflowExecution", DecisionType.CancelWorkflowExecution},
                new Object[]{"CompleteWorkflowExecution", DecisionType.CompleteWorkflowExecution}
                // TODO: revisit in Issue #11
//                new Object[]{"ContinueAsNewWorkflowExecution", DecisionType.ContinueAsNewWorkflowExecution}
        };
    }

    @Test
    @Parameters(method = "closeDecisions")
    public void retryScheduledAfterClose(String name, DecisionType closeDecision) throws Exception {
        workflowId = "retryScheduledAfter-" + name;
        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CLOSE_WF_SIGNAL, closeDecision);

        sleep(Duration.ofSeconds(10));
        assertThat(decider.isStarted()).isTrue(); // case #1
        waitForWorkflowToClose(5); // case #2
    }

    /**
     * In this test the activity is scheduled after cancel. This is the definite way that leads to the case 1.
     * <p>
     * The tests {@link #retryScheduledAfterClose} and {@link #retryScheduledAfterCancel()}
     * emulate real-life situations and don't guarantee outcome (could be case 1 or case 2).
     */
    @Test
    public void scheduledActivityAfterCancel() throws Exception {
        workflowId = "case1_cancel";
        doNothing().when(template).onStart(any());
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.cancelWorkflow("Cancel")
                    .scheduleActivityTask(DefaultActivity.class, "");
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    public void scheduledActivityAfterFail() throws Exception {
        workflowId = "case1_fail";
        doNothing().when(template).onStart(any());
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.failWorkflow("Fail", "Deliberately")
                    .scheduleActivityTask(DefaultActivity.class, "");
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    public void scheduledActivityAfterComplete() throws Exception {
        workflowId = "case1_complete";
        doNothing().when(template).onStart(any());
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.completeWorkflow()
                    .scheduleActivityTask(DefaultActivity.class, "");
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    /**
     * TODO: enable when issue #11 is fixed, now there is no way to terminate new WF
     */
    @Ignore
    @Test
    public void scheduledActivityAfterContinueAsNew() throws Exception {
        workflowId = "case1_ContinueAsNew";

        doNothing().when(template).onStart(any());
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.continueAsNewWorkflow("1")
                    .scheduleActivityTask(DefaultActivity.class, "");
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    // test decisions
    private static final UnaryOperator<Decisions> recordMarker = decideTo -> decideTo.recordMarker("My Marker");
    private static final UnaryOperator<Decisions> scheduleActivity = decideTo -> decideTo.scheduleActivityTask(DefaultActivity.class, "");
    private static final UnaryOperator<Decisions> startTimer = decideTo -> decideTo.startTimer("Timer", Duration.ofHours(5));
    private static final UnaryOperator<Decisions> cancelTimer = decideTo -> decideTo.cancelTimer(CANCEL_TIMER);
    private static final UnaryOperator<Decisions> startChildWorkflow = decideTo -> decideTo.startChildWorkflow(RetryWorkflow.class, "new");
    private static final UnaryOperator<Decisions> requestCancelExternalWorkflow = decideTo -> decideTo.requestCancelExternalWorkflow("wf", "some run id");

    // on start decisions
    private static final Consumer<Decisions> doNothing = decideTo -> {};
    private static final Consumer<Decisions> startTimerToCancel = decideTo -> decideTo.startTimer(CANCEL_TIMER, Duration.ofMinutes(10));

    private Object[] decisions() {
        return new Object[]{
                new Object[]{"recordMarker", recordMarker, doNothing},
                new Object[]{"scheduleActivity", scheduleActivity, doNothing},
                new Object[]{"startTimer", startTimer, doNothing},
                new Object[]{"cancelTimer", cancelTimer, startTimerToCancel},
                new Object[]{"startChildWorkflow", startChildWorkflow, doNothing},
                new Object[]{"requestCancelExternalWorkflow", requestCancelExternalWorkflow, doNothing}
        };
    }

    @Test
    @Parameters(method = "decisions")
    public void scheduleDecisionBeforeCancel(String name, UnaryOperator<Decisions> decision, Consumer<Decisions> onStart) throws Exception {
        workflowId = "case1-" + name + "-before-cancel";
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            onStart.accept(decideTo);
            return null;
        }).when(template).onStart(any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decision.apply(decideTo).cancelWorkflow("cancel");
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    @Parameters(method = "decisions")
    public void scheduleDecisionAfterCancel(String name, UnaryOperator<Decisions> decision, Consumer<Decisions> onStart) throws Exception {
        workflowId = "case1-" + name + "-after-cancel";
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            onStart.accept(decideTo);
            return null;
        }).when(template).onStart(any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.cancelWorkflow("cancel");
            decision.apply(decideTo);
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    @Parameters(method = "decisions")
    public void scheduleDecisionBeforeComplete(String name, UnaryOperator<Decisions> decision, Consumer<Decisions> onStart) throws Exception {
        workflowId = "case1-" + name + "-before-complete";
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            onStart.accept(decideTo);
            return null;
        }).when(template).onStart(any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decision.apply(decideTo).completeWorkflow();
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    @Parameters(method = "decisions")
    public void scheduleDecisionAfterComplete(String name, UnaryOperator<Decisions> decision, Consumer<Decisions> onStart) throws Exception {
        workflowId = "case1-" + name + "-after-complete";
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            onStart.accept(decideTo);
            return null;
        }).when(template).onStart(any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.completeWorkflow();
            decision.apply(decideTo);
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    @Parameters(method = "decisions")
    public void scheduleDecisionBeforeFail(String name, UnaryOperator<Decisions> decision, Consumer<Decisions> onStart) throws Exception {
        workflowId = "case1-" + name + "-before-fail";

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            onStart.accept(decideTo);
            return null;
        }).when(template).onStart(any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decision.apply(decideTo).failWorkflow("deliberate failure", "deliberate failure");
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    @Parameters(method = "decisions")
    public void scheduleDecisionAfterFail(String name, UnaryOperator<Decisions> decision, Consumer<Decisions> onStart) throws Exception {
        workflowId = "case1-" + name + "-after-fail";

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            onStart.accept(decideTo);
            return null;
        }).when(template).onStart(any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.failWorkflow("deliberate failure", "deliberate failure");
            decision.apply(decideTo);
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    /**
     * TODO: enable when issue #11 is fixed, now there is no way to terminate new WF
     */
    @Ignore
    @Test
    @Parameters(method = "decisions")
    public void scheduleDecisionBeforeContinueAsNew(String name, UnaryOperator<Decisions> decision, Consumer<Decisions> onStart) throws Exception {
        workflowId = "case1-" + name + "-before-continue-as-new";

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            onStart.accept(decideTo);
            return null;
        }).when(template).onStart(any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decision.apply(decideTo).continueAsNewWorkflow("1");
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    /**
     * TODO: enable when issue #11 is fixed, now there is no way to terminate new WF
     */
    @Ignore
    @Test
    @Parameters(method = "decisions")
    public void scheduleDecisionAfterContinueAsNew(String name, UnaryOperator<Decisions> decision, Consumer<Decisions> onStart) throws Exception {
        workflowId = "case1-" + name + "-after-continue-as-new";

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            onStart.accept(decideTo);
            return null;
        }).when(template).onStart(any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.continueAsNewWorkflow("1");
            decision.apply(decideTo);
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    public void severalCloseDecisions() throws Exception {
        workflowId = "case1_several-close-decisions";

        doNothing().when(template).onStart(any());
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.completeWorkflow().cancelWorkflow("").failWorkflow("", "").continueAsNewWorkflow("1");
            return null;
        }).when(template).onCase1SignalReceived(any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @After
    public void tearDown() throws Exception {
        LOGGER.info("Teardown starts");
        if (workflowId != null && runId != null) {
            if (swiffer.isWorkflowExecutionOpen(workflowId, runId)) {
                try {
                    swiffer.terminateWorkflow(workflowId, runId, "Terminate running workflow at the end of the test");
                } catch (Exception e) {
                    LOGGER.error("Error during WF termination", e);
                }
            }
        }

        workflowId = null;
        runId = null;

        reset(template, executor);
        LOGGER.info("Teardown ends");
    }

    @BeforeClass
    public static void setUp() throws Exception {
        AmazonSimpleWorkflow amazonSimpleWorkflow = new AmazonSimpleWorkflowClient(
                new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.EU_WEST_1);

        swiffer = new Swiffer(amazonSimpleWorkflow, DOMAIN);

        worker = swiffer.newWorkerBuilder()
                        .taskList(TASK_LIST)
                        .identity("worker")
                        .executors((Object) executor)
                        .build();

        decider = swiffer.newDeciderBuilder()
                         .taskList(TASK_LIST)
                         .identity("decider")
                         .workflowTemplates((Object) template)
                         .globalRetryPolicy(GLOBAL_RETRY_POLICY)
                         .build();

        worker.start();
        decider.start();
    }

    @AfterClass
    public static void stopAll() {
        try {
            stop(decider);
            stop(worker);
        } catch (Exception e) {
            LOGGER.error("Error stopping the deciders/workers", e);
        }
    }

    private static void stop(TaskListService service) {
        if (service != null) {
            service.stop();
        }
    }

    private void sleep(Duration duration) throws InterruptedException {
        Thread.sleep(duration.toMillis());
    }

    private void waitForWorkflowToClose(int maxSeconds) throws InterruptedException {
        long end = System.currentTimeMillis() + maxSeconds * 1000;
        boolean started = swiffer.isWorkflowExecutionOpen(workflowId, runId);
        while (started) {
            if (System.currentTimeMillis() > end) {
                fail("Workflow was not closed in the max allowed time.");
            }
            sleep(Duration.ofSeconds(2));
            started = swiffer.isWorkflowExecutionOpen(workflowId, runId);
        }
    }
}
