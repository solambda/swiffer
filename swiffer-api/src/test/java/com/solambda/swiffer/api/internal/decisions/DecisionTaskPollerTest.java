package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.junit.Test;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.PollForDecisionTaskRequest;
import com.amazonaws.services.simpleworkflow.model.TaskList;


public class DecisionTaskPollerTest {

    private static final String DOMAIN = "domain";
    private static final String TASK_LIST = "task-list";
    private static final String DECISIDER = "decision-poller";

    @Test
    public void pollForTask() throws Exception {
        AmazonSimpleWorkflow swf = mock(AmazonSimpleWorkflow.class);
        String nextPageToken = "nextPage";
        HistoryEvent first = mock(HistoryEvent.class);
        when(first.getEventId()).thenReturn(1L);
        List<HistoryEvent> firstPageEvents = generateRandomHistoryEvents(11, 1010);
        List<HistoryEvent> secondPageEvents = generateRandomHistoryEvents(1, 10);

        DecisionTask firstPage = new DecisionTask().withTaskToken("TOKEN")
                                                       .withNextPageToken(nextPageToken)
                                                       .withEvents(firstPageEvents);

        DecisionTask secondPage = new DecisionTask().withTaskToken("TOKEN")
                                                         .withEvents(secondPageEvents);

        PollForDecisionTaskRequest firstRequest = getRequest(null);
        PollForDecisionTaskRequest secondRequest = getRequest(nextPageToken);

        when(swf.pollForDecisionTask(eq(firstRequest))).thenReturn(firstPage);
        when(swf.pollForDecisionTask(eq(secondRequest))).thenReturn(secondPage);

        DecisionTaskPoller poller = new DecisionTaskPoller(swf, DOMAIN, TASK_LIST, DECISIDER);

        DecisionTaskContext context = poller.poll();

        verify(swf).pollForDecisionTask(eq(firstRequest));
        verify(swf).pollForDecisionTask(eq(secondRequest));

        List<Long> expectedId = LongStream.iterate(1010, operand -> --operand).limit(1010).boxed().collect(Collectors.toList());
        assertThat(context.history().events()).extracting(WorkflowEvent::id).containsExactlyElementsOf(expectedId);
    }

    private List<HistoryEvent> generateRandomHistoryEvents(int start, int end){
        return LongStream.rangeClosed(start, end).mapToObj(value -> {
            HistoryEvent historyEvent = mock(HistoryEvent.class);
            when(historyEvent.getEventId()).thenReturn(value);
            return historyEvent;
        }).sorted(Comparator.comparing(HistoryEvent::getEventId).reversed()).collect(Collectors.toList());
    }

    private PollForDecisionTaskRequest getRequest(String nextPageToken){
        return new PollForDecisionTaskRequest().withDomain(DOMAIN)
                                               .withTaskList(new TaskList().withName(TASK_LIST))
                                               .withReverseOrder(true)
                                               .withIdentity(DECISIDER)
                                               .withNextPageToken(nextPageToken);
    }
}