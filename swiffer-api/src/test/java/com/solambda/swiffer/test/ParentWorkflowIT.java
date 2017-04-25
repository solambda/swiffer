package com.solambda.swiffer.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflowClient;
import com.amazonaws.services.simpleworkflow.model.ChildPolicy;
import com.amazonaws.services.simpleworkflow.model.StartChildWorkflowExecutionFailedCause;
import com.amazonaws.services.simpleworkflow.model.WorkflowExecutionCancelRequestedCause;
import com.solambda.swiffer.api.*;
import com.solambda.swiffer.api.internal.decisions.DecisionTaskContext;

/**
 * Integration test for interactions between parent/child workflows.
 */
public class ParentWorkflowIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParentWorkflowIT.class);

    private static final String PARENT_TASK_LIST = "parent-task-list";
    private static final String CHILD_TASK_LIST = "child-task-list";

    @Retention(RetentionPolicy.RUNTIME)
    @WorkflowType(name = "ParentWorkflowIT-parent", version = "2", defaultTaskList = PARENT_TASK_LIST)
    public @interface ParentWorkflow {

    }

    @ActivityType(name = "ParentWorkflowIT-parent-activity", version = "2", defaultTaskList = PARENT_TASK_LIST)
    public @interface ParentActivity {
    }

    public static class ParentActivityExecutor {

        @Executor(activity = ParentActivity.class)
        public void executeTestActivity() {
            LOGGER.info("Parent activity executed");
        }
    }

    @ParentWorkflow
    public static class ParentWorkflowTemplate {

        @OnWorkflowStarted
        public void onStart(Decisions decisions) {
            decisions.scheduleActivityTask(ParentActivity.class, "");
        }

        @OnActivityCompleted(ParentActivity.class)
        public void onActivityCompleted(Decisions decisions) {
            // do nothing, this is intended to be mocked
        }

        @OnChildWorkflowCanceled(ChildWorkflow.class)
        public void onChildWorkflowCanceled(String details, Decisions decideTo, DecisionTaskContext context) {
            LOGGER.info("OnChildWorkflowCanceled event with default parameter " + details);
        }

        @OnChildWorkflowCompleted(ChildWorkflow.class)
        public void onChildWorkflowCompleted(@Output Long result, Decisions decideTo, DecisionTaskContext context) {
            LOGGER.info("OnChildWorkflowCompleted event with default parameter " + result);
        }

        @OnChildWorkflowFailed(ChildWorkflow.class)
        public void onChildWorkflowFailed(@Reason String reason, Decisions decideTo, DecisionTaskContext context ) {
            LOGGER.info("OnChildWorkflowFailed event with default parameter " + reason);
        }

        @OnChildWorkflowStarted(ChildWorkflow.class)
        public void onChildWorkflowStarted(String runId, Decisions decideTo, DecisionTaskContext context) {
            childRunId = runId;
            LOGGER.info("OnChildWorkflowStarted event with default parameter " + runId);
        }

        @OnChildWorkflowTerminated(ChildWorkflow.class)
        public void onChildWorkflowTerminated(Long initialEventId, Decisions decideTo, DecisionTaskContext context) {
            LOGGER.info("OnChildWorkflowTerminated event with default parameter " + initialEventId);
        }

        @OnChildWorkflowTimedOut(ChildWorkflow.class)
        public void onChildWorkflowTimedOut(Long initialEventId, Decisions decideTo, DecisionTaskContext context) {
            LOGGER.info("OnChildWorkflowTimedOut event with default parameter " + initialEventId);
        }

        @OnStartChildWorkflowFailed(ChildWorkflow.class)
        public void onStartChildWorkflowFailed(String cause, Decisions decideTo, DecisionTaskContext context) {
            LOGGER.info("OnStartChildWorkflowFailed event with default parameter " + cause);
        }

    }

    @Retention(RetentionPolicy.RUNTIME)
    @WorkflowType(name = "ParentWorkflowIT-child", version = "2", defaultTaskList = CHILD_TASK_LIST)
    public @interface ChildWorkflow {

    }

    @ActivityType(name = "ParentWorkflowIT-child-activity", version = "2", defaultTaskList = CHILD_TASK_LIST)
    public @interface ChildActivity {
    }

    public static class ChildActivityExecutor {

        @Executor(activity = ChildActivity.class)
        public void executeTestActivity() {
            LOGGER.info("Child activity executed");
        }

    }

    @ChildWorkflow
    public static class ChildWorkflowTemplate {

        @OnWorkflowStarted
        public void onStart(Decisions decisions) {
            decisions.scheduleActivityTask(ChildActivity.class, "");
        }

        @OnActivityCompleted(ChildActivity.class)
        public void onActivityCompleted(Decisions decisions) {
            // do nothing, this is intended to be mocked
        }

        @OnWorkflowCancelRequested
        public void onWorkflowCancelRequested(String cause, Decisions decideTo, DecisionTaskContext context) {
            LOGGER.info("OnWorkflowCancelRequested event with default parameter " + cause);
            decideTo.cancelWorkflow("Request to cancel received");
        }
    }

    private static final String DOMAIN = "github-swiffer";
    private static Swiffer swiffer;

    private static final String PARENT_WF_NAME = "[Parent WF] ";
    private static final ParentActivityExecutor parentExecutor = new ParentActivityExecutor();
    private static final ParentWorkflowTemplate parentTemplate = spy(new ParentWorkflowTemplate());
    private static Worker parentWorker;
    private static Decider parentDecider;
    private String parentRunId;
    private String parentWorkflowId;

    private static final String CHILD_WF_NAME = "[Child WF] ";
    private static final ChildActivityExecutor childExecutor = new ChildActivityExecutor();
    private static final ChildWorkflowTemplate childTemplate = spy(new ChildWorkflowTemplate());
    private static Worker childWorker;
    private static Decider childDecider;
    private static String childRunId;
    private String childWorkflowId;

    /**
     * Test case:
     * verify that {@link OnWorkflowCancelRequested} handler is called when parent workflow is completed
     * and the child policy is {@link ChildPolicy#REQUEST_CANCEL}.
     */
    @Test
    public void parentWorkflow_Completed() throws Exception {
        String testCase = "[Parent WF completed]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];

            decisions.scheduleActivityTask(ParentActivity.class, "")
                     .startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onStart(any());

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.completeWorkflow();
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, null, new WorkflowOptions().childTerminationPolicy(ChildPolicy.REQUEST_CANCEL), null);

        verify(childTemplate, timeout(5000)).onWorkflowCancelRequested(eq(WorkflowExecutionCancelRequestedCause.CHILD_POLICY_APPLIED.name()), any(), any());
    }

    /**
     * Test case:
     * verify that {@link OnWorkflowCancelRequested} handler is called when parent workflow fails
     * and the child policy is {@link ChildPolicy#REQUEST_CANCEL}.
     */
    @Test
    public void parentWorkflow_Fail() throws Exception {
        String testCase = "[Parent WF fails]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.scheduleActivityTask(ParentActivity.class, "")
                     .startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onStart(any());

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.failWorkflow("Deliberate Fail", "Deliberate Fail");
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, null, new WorkflowOptions().childTerminationPolicy(ChildPolicy.REQUEST_CANCEL), null);

        verify(childTemplate, timeout(5000)).onWorkflowCancelRequested(eq(WorkflowExecutionCancelRequestedCause.CHILD_POLICY_APPLIED.name()), any(), any());
    }

    /**
     * Test case:
     * verify that {@link OnWorkflowCancelRequested} handler is called when parent workflow is canceled
     * and the child policy is {@link ChildPolicy#REQUEST_CANCEL}.
     */
    @Test
    public void parentWorkflow_Cancel() throws Exception {
        String testCase = "[Parent WF cancel]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.scheduleActivityTask(ParentActivity.class, "")
                     .startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onStart(any());

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.cancelWorkflow("Cancel");

            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, null, new WorkflowOptions().childTerminationPolicy(ChildPolicy.REQUEST_CANCEL), null);

        verify(childTemplate, timeout(5000)).onWorkflowCancelRequested(eq(WorkflowExecutionCancelRequestedCause.CHILD_POLICY_APPLIED.name()), any(), any());
    }

    /**
     * Test case:
     * verify that {@link OnWorkflowCancelRequested} handler is called when parent workflow timed out
     * and the child policy is {@link ChildPolicy#REQUEST_CANCEL}.
     */
    @Test
    public void parentWorkflow_Timeout() throws Exception {
        String testCase = "[Parent WF timeout]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.scheduleActivityTask(ParentActivity.class, "")
                     .startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onStart(any());


        doAnswer(invocation -> {
            Tests.sleep(Duration.ofSeconds(2));
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, null, new WorkflowOptions().childTerminationPolicy(ChildPolicy.REQUEST_CANCEL)
                                                                                                               .maxWorkflowDuration(Duration.ofSeconds(1)), null);

        verify(childTemplate, timeout(5000)).onWorkflowCancelRequested(eq(WorkflowExecutionCancelRequestedCause.CHILD_POLICY_APPLIED.name()), any(), any());
    }

    /**
     * Test case:
     * verify that {@link OnWorkflowCancelRequested} handler is called when parent workflow is terminated
     * and the child policy is {@link ChildPolicy#REQUEST_CANCEL}.
     */
    @Test
    public void parentWorkflow_Terminate() throws Exception {
        String testCase = "[Parent WF terminated]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onStart(any());


        doAnswer(invocation -> {
            swiffer.terminateWorkflow(parentWorkflowId, parentRunId, "Deliberate Termination");
            return null;
        }).when(childTemplate).onStart(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, null, new WorkflowOptions().childTerminationPolicy(ChildPolicy.REQUEST_CANCEL), null);

        verify(childTemplate, timeout(3000)).onWorkflowCancelRequested(eq(WorkflowExecutionCancelRequestedCause.CHILD_POLICY_APPLIED.name()), any(), any());
    }

    @Test
    public void childWorkflow_Canceled() throws Exception {
        String testCase = "[Child WF canceled]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        String details = "Deliberate Cancel";

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.cancelWorkflow(details);
            return null;
        }).when(childTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, "");

        verify(parentTemplate, timeout(5000)).onChildWorkflowCanceled(eq(details), any(), any());
    }

    @Test
    public void childWorkflow_Completed() throws Exception {
        String testCase = "[Child WF completed]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        Long result = 2437L;
        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.completeWorkflow(result);
            return null;
        }).when(childTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, "");

        verify(parentTemplate, timeout(5000)).onChildWorkflowCompleted(eq(result), any(), any());
    }

    @Test
    public void childWorkflow_Failed() throws Exception {
        String testCase = "[Child WF failed]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        String reason = "Deliberate failure";

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.failWorkflow(reason, "Details");
            return null;
        }).when(childTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, "");

        verify(parentTemplate, timeout(5000)).onChildWorkflowFailed(eq(reason), any(), any());
    }

    @Test
    public void childWorkflow_Started() throws Exception {
        String testCase = "[Child WF started]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, "");

        ArgumentCaptor<String> runIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(parentTemplate, timeout(5000)).onChildWorkflowStarted(runIdCaptor.capture(), any(), any());
        assertThat(runIdCaptor.getValue()).isNotEmpty();
    }

    @Test
    public void childWorkflow_Terminated() throws Exception {
        String testCase = "[Child WF terminated]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId)
                     .scheduleActivityTask(ParentActivity.class, null);
            return null;
        }).when(parentTemplate).onStart(any());

        doAnswer(invocation -> {
            swiffer.terminateWorkflow(childWorkflowId, childRunId, "Deliberately terminate child");
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, "");

        ArgumentCaptor<Long> initialEventIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(parentTemplate, timeout(5000)).onChildWorkflowTerminated(initialEventIdCaptor.capture(), any(), any());
        assertThat(initialEventIdCaptor.getValue()).isGreaterThan(0);
    }

    @Test
    public void childWorkflow_TimedOut() throws Exception {
        String testCase = "[Child WF timed out]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId, null, new WorkflowOptions().maxWorkflowDuration(Duration.ofSeconds(1)));
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        doAnswer(invocation -> {
            Tests.sleep(Duration.ofSeconds(2));
            return null;
        }).when(childTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, "");

        ArgumentCaptor<Long> initialEventIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(parentTemplate, timeout(5000)).onChildWorkflowTimedOut(initialEventIdCaptor.capture(), any(), any());
        assertThat(initialEventIdCaptor.getValue()).isGreaterThan(0);
    }

    @Test
    public void childWorkflow_StartChildFailed() throws Exception {
        String testCase = "[Child WF fails to start]";
        parentWorkflowId = PARENT_WF_NAME + testCase;
        childWorkflowId = CHILD_WF_NAME + testCase;

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId)
                     .scheduleActivityTask(ParentActivity.class, null);
            return null;
        }).when(parentTemplate).onStart(any());

        doAnswer(invocation -> {
            Decisions decisions = (Decisions) invocation.getArguments()[0];
            decisions.startChildWorkflow(ChildWorkflow.class, childWorkflowId);
            return null;
        }).when(parentTemplate).onActivityCompleted(any());

        parentRunId = swiffer.startWorkflow(ParentWorkflow.class, parentWorkflowId, "");

        verify(parentTemplate, timeout(5000)).onStartChildWorkflowFailed(eq(StartChildWorkflowExecutionFailedCause.WORKFLOW_ALREADY_RUNNING.name()), any(), any());
    }

    @After
    public void tearDown() throws Exception {
        LOGGER.info("Teardown starts");
        terminateWorkflow(childWorkflowId, childRunId);
        terminateWorkflow(parentWorkflowId, parentRunId);

        parentWorkflowId = null;
        parentRunId = null;
        childWorkflowId = null;
        childRunId = null;

        Mockito.reset(parentTemplate, childTemplate);
        LOGGER.info("Teardown ends");
    }

    @BeforeClass
    public static void setUp() throws Exception {
        swiffer = getSwiffer();
        parentWorker = getWorker(swiffer, "parent-worker", PARENT_TASK_LIST, parentExecutor);
        parentDecider = getDecider(swiffer, "parent-decider", PARENT_TASK_LIST, parentTemplate);

        childWorker = getWorker(swiffer, "child-worker", CHILD_TASK_LIST, childExecutor);
        childDecider = getDecider(swiffer, "child-decider", CHILD_TASK_LIST, childTemplate);

        parentWorker.start();
        parentDecider.start();
        childWorker.start();
        childDecider.start();
    }

    @AfterClass
    public static void stopAll() {
        try {
            stop(childDecider);
            stop(childWorker);
            stop(parentDecider);
            stop(parentWorker);
        } catch (Exception e) {
            LOGGER.error("Error stopping the deciders/workers", e);
        }
    }

    private void terminateWorkflow(String workflowId, String runId) {
        if (workflowId != null && runId != null) {
            if (swiffer.isWorkflowExecutionOpen(workflowId, runId)) {
                try {
                    swiffer.terminateWorkflow(workflowId, runId, "Terminate running workflow at the end of the test");
                }
                catch (Exception e) {
                    LOGGER.error("Error during WF termination", e);
                }
            }
        }
    }

    private static void stop(TaskListService service) {
        if (service != null) {
            service.stop();
        }
    }

    private static Worker getWorker(Swiffer swiffer, String identity, String taskList, Object executor) {
        return swiffer.newWorkerBuilder()
                      .taskList(taskList)
                      .identity(identity)
                      .executors(executor)
                      .build();
    }

    private static Decider getDecider(Swiffer swiffer, String identity, String taskList, Object template) {
        return swiffer.newDeciderBuilder()
                      .taskList(taskList)
                      .identity(identity)
                      .workflowTemplates(template)
                      .build();
    }

    private static Swiffer getSwiffer() {
        AmazonSimpleWorkflow amazonSimpleWorkflow = new AmazonSimpleWorkflowClient(
                new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.EU_WEST_1);
        return new Swiffer(amazonSimpleWorkflow, DOMAIN);
    }
}
