package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.util.function.Predicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.Decider;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Executor;
import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.OnActivityCompleted;
import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.api.Output;
import com.solambda.swiffer.api.Swiffer;
import com.solambda.swiffer.api.Worker;
import com.solambda.swiffer.api.WorkflowType;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskPollerIT.Definitions.ExecuteDummyTask;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskPollerIT.Definitions.RequestHistory;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskPollerIT.Definitions.Workflow;
import com.solambda.swiffer.test.Tests;

/**
 * Integration Test for {@link DecisionTaskPoller}.
 * Uses connection to the real Amazon SWF.
 */
public class DecisionTaskPollerIT {

    private static final int DUMMY_EVENTS_NUMBER = 167;
    private static final String TASK_LIST = "history-test-task-list";
    private static final String WORKFLOW_ID = "historyTestWorkflow";
    private static final String DUMMY_TASK = "dummy-task";

    private Swiffer swiffer;
    private Worker worker;
    private Decider decider;

    private String runId;

    Predicate<WorkflowEvent> completedDummyTask = workflowEvent -> workflowEvent.type() == EventType.ActivityTaskCompleted
            && workflowEvent.activityType().name().equals(DUMMY_TASK);

    private boolean wfCompleted = false;

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

    // TODO: create Integration Test suite?
    @Ignore
    @Test
    public void pollForTask() throws Exception {
        runId = swiffer.startWorkflow(Workflow.class, WORKFLOW_ID, 0);

        Tests.sleep(Duration.ofMinutes(5));

        assertThat(wfCompleted).isTrue();
    }

    public static class Definitions {
        @ActivityType(name = "request-history", version="2", defaultTaskList = TASK_LIST)
        public @interface RequestHistory {}

        @ActivityType(name = DUMMY_TASK, version="2", defaultTaskList = TASK_LIST)
        public @interface ExecuteDummyTask {}

        @Retention(RetentionPolicy.RUNTIME)
        @WorkflowType(name = "long-history", version = "1", defaultTaskList = TASK_LIST)
        public @interface Workflow{}
    }

    public class ActivityExecutors {
        @Executor(activity = RequestHistory.class)
        public void requestHistory() {
            // do nothing
        }

        @Executor(activity = ExecuteDummyTask.class)
        public int executeDummyTask(@Input int counter) {
            return ++counter;
        }
    }

    @Workflow
    public class WorkflowTemplate {

        @OnWorkflowStarted
        public void onStart(@Input int counter, Decisions decideTo) {
            decideTo.scheduleActivityTask(ExecuteDummyTask.class, counter);
        }

        @OnActivityCompleted(ExecuteDummyTask.class)
        public void onDummyTaskExecuted(@Output int counter, Decisions decideTo) {
            if (counter < DUMMY_EVENTS_NUMBER) {
                decideTo.scheduleActivityTask(ExecuteDummyTask.class, counter);
            } else {
                decideTo.scheduleActivityTask(RequestHistory.class, null);
            }
        }

        @OnActivityCompleted(RequestHistory.class)
        public void onRequestHistoryCompleted(DecisionTaskContext context, Decisions decideTo) {
            WorkflowHistory history = context.history();
            int expectedHistorySize = DUMMY_EVENTS_NUMBER * 6 + 9;
            assertThat(history.events()).hasSize(expectedHistorySize);
            assertThat(history.events()).filteredOn(completedDummyTask).hasSize(DUMMY_EVENTS_NUMBER);

            decideTo.completeWorkflow();
            wfCompleted = true;
        }
    }

    private Worker createWorker() {
        return swiffer.newWorkerBuilder()
                      .identity("history-test-worker")
                      .taskList(TASK_LIST)
                      .executors(new ActivityExecutors())
                      .build();
    }

    private Decider createDecider() {
        return swiffer.newDeciderBuilder()
                      .taskList(TASK_LIST)
                      .identity("history-test-decider")
                      .workflowTemplates(new WorkflowTemplate())
                      .build();
    }
}