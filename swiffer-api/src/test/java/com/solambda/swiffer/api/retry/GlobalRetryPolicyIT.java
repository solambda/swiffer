package com.solambda.swiffer.api.retry;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.Decider;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Executor;
import com.solambda.swiffer.api.OnTimerFired;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.api.Swiffer;
import com.solambda.swiffer.api.Worker;
import com.solambda.swiffer.api.WorkflowType;
import com.solambda.swiffer.api.retry.GlobalRetryPolicyIT.Definitions.FailingActivity;
import com.solambda.swiffer.api.retry.GlobalRetryPolicyIT.Definitions.TimedOutActivity;
import com.solambda.swiffer.api.retry.GlobalRetryPolicyIT.Definitions.Workflow;
import com.solambda.swiffer.test.Tests;

/**
 * Integration test for {@link RetryPolicy}.
 */
public class GlobalRetryPolicyIT {

    private static final String TASK_LIST = "retry-policy-test-task-list";
    private static final String WORKFLOW_NAME = "GlobalRetryPolicyIT";
    private static final String WORKFLOW_ID = WORKFLOW_NAME + "_ID";
    private static final String FAILING_ACTIVITY = "failing-activity";
    private static final String TIMEOUT_ACTIVITY = "timeout-activity";
    private static final String COMPLETE_WORKFLOW_TIMER = "complete-workflow-timer";
    private static final int MAX_ATTEMPTS_NUMBER = 3;

    private Swiffer swiffer;
    private Worker worker;
    private Decider decider;
    private String runId;
    private boolean wfCompleted;
    private int timedOutRetried;
    private int failingRetried;

    private final RetryPolicy retryPolicy = new ConstantTimeRetryPolicy(Duration.ofSeconds(2), MAX_ATTEMPTS_NUMBER);

    @Before
    public void setup() {
        swiffer = new Swiffer(Tests.swf(), Tests.DOMAIN);
        worker = createWorker();
        decider = createDecider();

        worker.start();
        decider.start();
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (runId != null) {
                boolean isOpen = swiffer.isWorkflowExecutionOpen(WORKFLOW_ID, runId);
                if (isOpen) {
                    // terminate WF in case test failed and it wasn't completed
                    swiffer.terminateWorkflow(WORKFLOW_ID, runId, "Tear Down test");
                }
            }
        } finally {
            if (worker != null) {
                worker.stop();
            }
            if (decider != null) {
                decider.stop();
            }
        }
    }

    // TODO: create Integration Test suite
    @Ignore
    @Test
    public void testGlobalRetryPolicy() throws Exception {
        runId = swiffer.startWorkflow(Workflow.class, WORKFLOW_ID, 0);

        Tests.sleep(Duration.ofSeconds(15));

        assertThat(wfCompleted).isTrue();
        assertThat(failingRetried).as("Failed activity retries").isEqualTo(MAX_ATTEMPTS_NUMBER + 1);
        assertThat(timedOutRetried).as("Timed out activity retries").isEqualTo(MAX_ATTEMPTS_NUMBER + 1);
    }

    public static class Definitions {

        @ActivityType(name = FAILING_ACTIVITY, version="1", defaultTaskList = TASK_LIST)
        public @interface FailingActivity {}

        @ActivityType(name = TIMEOUT_ACTIVITY, version="1", defaultTaskList = TASK_LIST, defaultTaskScheduleToCloseTimeout = 1)
        public @interface TimedOutActivity {}

        @Retention(RetentionPolicy.RUNTIME)
        @WorkflowType(name = WORKFLOW_NAME, version = "1", defaultTaskList = TASK_LIST)
        public @interface Workflow{}
    }

    public class ActivityExecutors {
        @Executor(activity = FailingActivity.class)
        public void fail() {
            failingRetried++;
            throw new RuntimeException("Fail deliberately");
        }

        @Executor(activity = TimedOutActivity.class)
        public void timeout() {
            timedOutRetried++;
            Tests.sleep(Duration.ofMillis(1200));
        }
    }

    @Workflow
    public class WorkflowTemplate {

        @OnWorkflowStarted
        public void onStart(Decisions decideTo) {
            decideTo.scheduleActivityTask(TimedOutActivity.class, new Object())
                    .scheduleActivityTask(FailingActivity.class, new Object())
                    .startTimer(COMPLETE_WORKFLOW_TIMER, Duration.ofSeconds(13));
        }

        @OnTimerFired(COMPLETE_WORKFLOW_TIMER)
        public void onTimer(Decisions decideTo) {
            decideTo.completeWorkflow();
            wfCompleted = true;
        }
    }

    private Worker createWorker() {
        return swiffer.newWorkerBuilder()
                      .identity(WORKFLOW_NAME + "-worker")
                      .taskList(TASK_LIST)
                      .executors(new ActivityExecutors())
                      .build();
    }

    private Decider createDecider() {
        return swiffer.newDeciderBuilder()
                      .taskList(TASK_LIST)
                      .identity(WORKFLOW_NAME + "-decider")
                      .workflowTemplates(new WorkflowTemplate())
                      .globalRetryPolicy(retryPolicy)
                      .build();
    }
}
