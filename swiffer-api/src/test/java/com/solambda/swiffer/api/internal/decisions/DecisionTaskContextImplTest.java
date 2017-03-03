package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.DecisionTask;
import com.amazonaws.services.simpleworkflow.model.HistoryEvent;
import com.amazonaws.services.simpleworkflow.model.MarkerRecordedEventAttributes;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.mapper.JacksonDataMapper;

/**
 * Test for {@link DecisionTaskContextImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class DecisionTaskContextImplTest {
    private static final String MARKER_NAME = "marker";
    private static final String domain = "domain";

    @Mock
    private AmazonSimpleWorkflow swf;
    @Mock
    private DecisionTask decisionTask;

    private final DataMapper dataMapper = new JacksonDataMapper();

    @Test
    public void getMarkerDetails() throws Exception {
        TestObject lastDetails = new TestObject("Details", 10);
        TestObject oldDetails = new TestObject("Details", 1);
        List<HistoryEvent> events = mockHistoryEvents(mockMarkerRecordedEvent(10L, MARKER_NAME, dataMapper.serialize(oldDetails)),
                                                      mockMarkerRecordedEvent(101L, MARKER_NAME, dataMapper.serialize(lastDetails)),
                                                      mockMarkerRecordedEvent(100L, "another-marker", "not details"));
        when(decisionTask.getEvents()).thenReturn(events);

        DecisionTaskContextImpl context = new DecisionTaskContextImpl(swf, domain, decisionTask, dataMapper);

        Optional<TestObject> marker = context.getMarkerDetails(MARKER_NAME, TestObject.class);
        TestObject complexJavaObject = marker.get();
        assertThat(complexJavaObject).isEqualTo(lastDetails);
    }

    @Test
    public void getMarkerDetails_NoMarker() throws Exception {
        List<HistoryEvent> events = mockHistoryEvents(mockMarkerRecordedEvent(100L, "another-marker", "not details"));
        when(decisionTask.getEvents()).thenReturn(events);

        DecisionTaskContextImpl context = new DecisionTaskContextImpl(swf, domain, decisionTask, dataMapper);

        Optional<TestObject> marker = context.getMarkerDetails(MARKER_NAME, TestObject.class);

        assertThat(marker.isPresent()).isFalse();
    }

    @Test
    public void getMarkerDetails_EmptyDetails() throws Exception {
        List<HistoryEvent> events = mockHistoryEvents(mockMarkerRecordedEvent(10L, MARKER_NAME, null));
        when(decisionTask.getEvents()).thenReturn(events);

        DecisionTaskContextImpl context = new DecisionTaskContextImpl(swf, domain, decisionTask, dataMapper);

        Optional<TestObject> marker = context.getMarkerDetails(MARKER_NAME, TestObject.class);

        assertThat(marker.isPresent()).isFalse();
    }

    @Test
    public void hasMarker() throws Exception {
        List<HistoryEvent> events = mockHistoryEvents(mockMarkerRecordedEvent(130L, "another-marker", null),
                                                      mockMarkerRecordedEvent(100L, MARKER_NAME, null));
        when(decisionTask.getEvents()).thenReturn(events);

        DecisionTaskContextImpl context = new DecisionTaskContextImpl(swf, domain, decisionTask, dataMapper);

        boolean result = context.hasMarker(MARKER_NAME);

        assertThat(result).isTrue();
    }

    @Test
    public void hasMarker_noMarker() throws Exception {
        List<HistoryEvent> events = mockHistoryEvents(mockMarkerRecordedEvent(130L, "another-marker", null));

        when(decisionTask.getEvents()).thenReturn(events);

        DecisionTaskContextImpl context = new DecisionTaskContextImpl(swf, domain, decisionTask, dataMapper);
        boolean result = context.hasMarker(MARKER_NAME);

        assertThat(result).isFalse();
    }

    private List<HistoryEvent> mockHistoryEvents(HistoryEvent... concreteEvents) {
        Stream<HistoryEvent> randomHistoryEvents = new Random().longs(10).mapToObj(value -> {
            HistoryEvent historyEvent = mock(HistoryEvent.class);
            when(historyEvent.getEventId()).thenReturn(value);
            return historyEvent;
        });
        return Stream.concat(randomHistoryEvents, Stream.of(concreteEvents))
                     .sorted(Comparator.comparing(HistoryEvent::getEventId).reversed())
                     .collect(Collectors.toList());
    }

    private HistoryEvent mockMarkerRecordedEvent(Long id, String name, String details){
        MarkerRecordedEventAttributes attributes = mock(MarkerRecordedEventAttributes.class);
        when(attributes.getMarkerName()).thenReturn(name);
        when(attributes.getDetails()).thenReturn(details);

        HistoryEvent event = mock(HistoryEvent.class);
        when(event.getEventId()).thenReturn(id);
        when(event.getMarkerRecordedEventAttributes()).thenReturn(attributes);

        return event;
    }

    static class TestObject {
        private final String name;
        private final Integer value;

        TestObject(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        TestObject() {
            this("", null);
        }

        public String getName() {
            return name;
        }

        public Integer getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }
    }
}