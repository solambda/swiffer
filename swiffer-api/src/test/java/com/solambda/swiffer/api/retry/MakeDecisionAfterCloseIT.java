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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.AmazonSimpleWorkflowException;
import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.*;

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
public class MakeDecisionAfterCloseIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(MakeDecisionAfterCloseIT.class);
    private static final String TASK_LIST = "MakeDecisionAfterCloseIT-task-list";
    private static final String COMPLETE_WF_SIGNAL = "MakeDecisionAfterCloseIT-complete-wf-signal";
    private static final String CASE1_WF_SIGNAL = "MakeDecisionAfterCloseIT-case1-wf-signal";
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

        @OnSignalReceived(COMPLETE_WF_SIGNAL)
        public void onCompleteWFSignalReceived(Decisions decideTo) throws InterruptedException {
            LOGGER.info("Signal received.");
            decideTo.completeWorkflow();
            Thread.sleep(8 * 1000);
            LOGGER.info("Complete workflow decision send.");
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

    @Test
    public void retryScheduledAfterComplete() throws Exception {
        workflowId = "retryScheduledAfterComplete";
        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, COMPLETE_WF_SIGNAL);

        sleep(Duration.ofSeconds(10));
        assertThat(decider.isStarted()).isTrue(); // case #1
        waitForWorkflowToClose(5); // case #2
    }

    /**
     * In this test the activity is scheduled after cancel. This is the definite way that leads to the case 1.
     * <p>
     * The tests {@link #retryScheduledAfterComplete} and {@link #retryScheduledAfterCancel()}
     * emulate real-life situations and don't guarantee outcome (could be case 1 or case 2).
     */
    @Test
    public void scheduledActivityAfterCancel() throws Exception {
        workflowId = "case1_cancel";
        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        doNothing().when(template).onStart(any());
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.cancelWorkflow("Cancel")
                    .scheduleActivityTask(DefaultActivity.class, "");
            return null;
        }).when(template).onCase1SignalReceived(any());

        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    public void scheduledActivityAfterFail() throws Exception {
        workflowId = "case1_fail";
        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        doNothing().when(template).onStart(any());
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.failWorkflow("Fail", "Deliberatly")
                    .scheduleActivityTask(DefaultActivity.class, "");
            return null;
        }).when(template).onCase1SignalReceived(any());

        sleep(Duration.ofSeconds(3));

        swiffer.sendSignalToWorkflow(workflowId, CASE1_WF_SIGNAL);

        waitForWorkflowToClose(10); // case #2
        assertThat(decider.isStarted()).isTrue(); // case #1
    }

    @Test
    public void scheduledActivityAfterComplete() throws Exception {
        workflowId = "case1_complete";
        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        doNothing().when(template).onStart(any());
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.completeWorkflow()
                    .scheduleActivityTask(DefaultActivity.class, "");
            return null;
        }).when(template).onCase1SignalReceived(any());

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
