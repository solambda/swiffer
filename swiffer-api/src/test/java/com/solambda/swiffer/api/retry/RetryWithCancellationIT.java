package com.solambda.swiffer.api.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.verification.VerificationWithTimeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.solambda.swiffer.api.*;
import com.solambda.swiffer.api.internal.context.ActivityTaskFailedContext;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskContext;

/**
 * When decisions are added after cancel,
 * then error "Close must be last decision in list" is reported by SWF.
 * <p>
 * This test ensures that automatic retry of failed/timed out activities does not lead to that situation.
 * Expected behaviour: If request to cancel workflow is received all automatic reties will be cancelled.
 */
public class RetryWithCancellationIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryWithCancellationIT.class);
    private static final String TASK_LIST = "RetryWithCancellationIT-task-list";
    private static final RetryPolicy GLOBAL_RETRY_POLICY = new ConstantTimeRetryPolicy(Duration.ofSeconds(3));
    private static final VerificationWithTimeout VERIFICATION_TIMEOUT = timeout(10 * 1000);

    @Retention(RetentionPolicy.RUNTIME)
    @WorkflowType(name = "RetryWithCancellationIT-workflow", version = "1", defaultTaskList = TASK_LIST)
    public static @interface RetryWorkflow{}

    @ActivityType(name = "RetryWithCancellationIT-failing-activity", version = "1", defaultTaskList = TASK_LIST)
    public static @interface FailingActivity {
    }

    @ActivityType(name = "RetryWithCancellationIT-failing-activity-with-handler", version = "1", defaultTaskList = TASK_LIST)
    public static @interface FailingActivityWithHandler {
    }

    @ActivityType(name = "RetryWithCancellationIT-default-activity", version = "1", defaultTaskList = TASK_LIST)
    public static @interface DefaultActivity {
    }

    @ActivityType(name = "RetryWithCancellationIT-special-activity", version = "1", defaultTaskList = TASK_LIST)
    public static @interface SpecialActivity {
    }

    @RetryWorkflow
    public static class RetryWorkflowTemplate {

        @OnWorkflowStarted
        public void onStart(Decisions decideTo, DecisionTaskContext context) {
            // this is intended to be mocked
        }

        @OnActivityCompleted(DefaultActivity.class)
        public void onDefaultActivity(Object input, Decisions decideTo, DecisionTaskContext context) {
            decideTo.cancelWorkflow("Cancel Workflow");
        }

        @OnActivityCompleted(SpecialActivity.class)
        public void onSpecialActivity(Object input, Decisions decideTo, DecisionTaskContext context) {
            decideTo.scheduleActivityTask(FailingActivityWithHandler.class, "")
                    .scheduleActivityTask(DefaultActivity.class, 8);
        }

        @OnActivityFailed(activity = FailingActivityWithHandler.class)
        public void onFailingActivityWithHandlerFailed(Long eventId, Decisions decideTo, ActivityTaskFailedContext context) {
            decideTo.retryActivity(eventId, context);
        }

        @OnWorkflowCancelRequested
        public void onCancelRequested(String cause, Decisions decideTo, DecisionTaskContext context) {
            decideTo.scheduleActivityTask(DefaultActivity.class, 8);
        }
    }

    public static class Executors {

        @Executor(activity = FailingActivity.class)
        public void failingActivity() {
            throw new RuntimeException("This activity is always failing, retried by default");
        }

        @Executor(activity = FailingActivityWithHandler.class)
        public void failingActivityWithHandler() {
            throw new RuntimeException("This activity is always failing, has handler");
        }

        @Executor(activity = DefaultActivity.class)
        public void defaultActivity(int seconds) throws InterruptedException {
            LOGGER.info("Default activity: sleep for {} seconds", seconds);
            Thread.sleep(seconds * 1000);
        }

        @Executor(activity = SpecialActivity.class)
        public void specialActivity() throws InterruptedException {
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

    /**
     * Test case:
     * Activity that fails has no failure handler and is retried by default using global retry policy.
     */
    @Test
    public void defaultRetry() throws Exception {
        workflowId = "defaultRetry";

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.scheduleActivityTask(FailingActivity.class, "");
            return null;
        }).when(template).onStart(any(), any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        verify(executor, VERIFICATION_TIMEOUT.times(2)).failingActivity(); // allow at least one retry

        swiffer.cancelWorkflow(workflowId, runId);

        waitForCancel(15);
        verify(executor, times(2)).failingActivity();
        assertThat(decider.isStarted()).isTrue();
    }

    /**
     * Test case:
     * Activity that fails has failure handler which schedules retry (with global retry policy).
     */
    @Test
    public void customHandler() throws Exception {
        workflowId = "customHandler";

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.scheduleActivityTask(FailingActivityWithHandler.class, "");
            return null;
        }).when(template).onStart(any(), any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        verify(executor, timeout(10 * 1000).times(2)).failingActivityWithHandler(); // allow at least one retry

        swiffer.cancelWorkflow(workflowId, runId);

        waitForCancel(15);
        verify(executor, times(2)).failingActivityWithHandler();
        assertThat(decider.isStarted()).isTrue();
    }

    /**
     * Test case:
     * Activity that fails has failure handler which schedules retry using custom retry policy.
     */
    @Test
    public void customRetryPolicy() throws Exception {
        workflowId = "customRetryPolicy";
        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.scheduleActivityTask(FailingActivityWithHandler.class, "");
            return null;
        }).when(template).onStart(any(), any());

        doAnswer(invocation -> {
            Long eventId = (Long) invocation.getArguments()[0];
            Decisions decideTo = (Decisions) invocation.getArguments()[1];
            ActivityTaskFailedContext context = (ActivityTaskFailedContext) invocation.getArguments()[2];
            decideTo.retryActivity(eventId, context, new ConstantTimeRetryPolicy(Duration.ofSeconds(1)));
            return null;
        }).when(template).onFailingActivityWithHandlerFailed(any(), any(), any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        verify(executor, timeout(10 * 1000).times(2)).failingActivityWithHandler();

        swiffer.cancelWorkflow(workflowId, runId);

        waitForCancel(15);
        verify(executor, times(2)).failingActivityWithHandler();
        assertThat(decider.isStarted()).isTrue();
    }

    /**
     * Test case:
     * Failing activity is scheduled after request for cancel received.
     */
    @Test
    public void failOnCancel() throws Exception {
        workflowId = "failOnCancel";

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[0];
            decideTo.scheduleActivityTask(FailingActivity.class, "");
            return null;
        }).when(template).onStart(any(), any());

        doAnswer(invocation -> {
            Decisions decideTo = (Decisions) invocation.getArguments()[1];
            decideTo.scheduleActivityTask(SpecialActivity.class, "");
            return null;
        }).when(template).onCancelRequested(any(), any(), any());

        runId = swiffer.startWorkflow(RetryWorkflow.class, workflowId, null);
        verify(executor, VERIFICATION_TIMEOUT.times(2)).failingActivity(); // allow at least one retry

        swiffer.cancelWorkflow(workflowId, runId);

        waitForCancel(15);
        verify(executor, times(2)).failingActivity();
        verify(executor, times(1)).failingActivityWithHandler();
        assertThat(decider.isStarted()).isTrue();
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

    private void waitForCancel(int maxSeconds) throws InterruptedException {
        long end = System.currentTimeMillis() + maxSeconds * 1000;
        boolean started = swiffer.isWorkflowExecutionOpen(workflowId, runId);
        while (started) {
            if (System.currentTimeMillis() > end) {
                fail("Workflow was not cancelled in the max allowed time.");
            }
            sleep(Duration.ofSeconds(2));
            started = swiffer.isWorkflowExecutionOpen(workflowId, runId);
        }
    }
}
