package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.Control;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.Marker;
import com.solambda.swiffer.api.Output;
import com.solambda.swiffer.api.Reason;
import com.solambda.swiffer.api.duration.DefaultDurationTransformer;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.mapper.JacksonDataMapper;

public class EventHandlerArgumentsProviderFactoryTest {
	private static final EventType ANY_TYPE = null;
	private static final String CAUSE = "cause";
	private static final String CONTROL = "control";
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	private static final String REASON = "reason";
	private static final String DETAILS = "details";
    private static final String MARKER_DETAILS = "markerDetails";
	private static final Long INITIAL_EVENT_ID = 5L;
	private static final String RUN_ID = "22OSeHwaQlI9DvOdRmlQpTZgtytsM7g73NUnoR5+aaSXc=";

	private static final String MARKER_NAME = "marker";
    private static final String NO_DETAILS_MARKER_NAME = "markerNoDetails";

	private final Decisions decisions = new DecisionsImpl(new JacksonDataMapper(), new DefaultDurationTransformer());
    private final DataMapper dataMapper = new JacksonDataMapper();

	@ActivityType(name = "activity1", version = "1")
	public static interface ActivityDef {

	}

	@SuppressWarnings("unused")
	private static class Template1 {

		/**
		 * method for asserting a handler can be called without any argument
		 */
		public void noArgumentMethod() {

		}

		/**
		 * used to assert the specific arguments are provided, whatever the type
		 * is
		 */
		public void specificParameterTypes(final Decisions decisions, final WorkflowEvent event,
				final EventContext context, final WorkflowHistory history) {

		}

		/**
		 * used to assert the default argument for a given EventType is provided
		 *
		 */
		public void defaultParameter(final String defaultArgument) {

		}

		/**
		 * used to assert the default parameters cannot be multiple
		 *
		 */
		public void defaultParametersCannotBeMultiple(final String input, final String output) {

		}

		public void specificAnnotations(
                final @Input String input,
                final @Output String output,
                final @Reason String reason,
                final @Control String control,
                @Marker(MARKER_NAME) String marker,
                @Marker(NO_DETAILS_MARKER_NAME) String noDetailsMarker) {

		}

		public void mixingEverything(
				/* specific types */
				final Decisions decisions,
				final WorkflowEvent event,
				final EventContext context,
				final WorkflowHistory history,
				/* specific annotations */
				final @Input String input,
				final @Output String output,
				final @Reason String reason,
				final @Control String control) {

		}

	}

	@Test
	public void noArgumentMethod() throws Exception {
		// GIVEN a method
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory(dataMapper);
		final Method method = getMethod("noArgumentMethod");
		// WHEN a ArgumentsProvider is created
		final EventHandlerArgumentsProvider provider = factory
				.createArgumentsProvider(ANY_TYPE, method);
		// THEN when used
		final EventContext context = createMockedContext();

		final Object[] arguments = provider.getArguments(context, this.decisions);
		verifyZeroInteractions(context);
		assertThat(arguments).isEmpty();
	}

	@Test
	public void parameterOfSpecificTypes() throws Exception {
		// GIVEN a method
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory(dataMapper);
		final Method method = getMethod("specificParameterTypes");
		// WHEN a ArgumentsProvider is created
		final EventHandlerArgumentsProvider provider = factory
				.createArgumentsProvider(ANY_TYPE, method);
		// THEN when used
		final EventContext context = createMockedContext();
		final Object[] arguments = provider.getArguments(context, this.decisions);
		assertThat(arguments).containsExactly(this.decisions, context.event(), context, context.history());

	}

	@Test
	public void parameterWithSpecificAnnotations() throws Exception {
		// GIVEN a method
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory(dataMapper);
		final Method method = getMethod("specificAnnotations");
		// WHEN a ArgumentsProvider is created
		final EventHandlerArgumentsProvider provider = factory
				.createArgumentsProvider(ANY_TYPE, method);
		// THEN when used
		final EventContext context = createMockedContext();
		final Object[] arguments = provider.getArguments(context, this.decisions);
		verify(context.event(), times(1)).input();
		verify(context.event(), times(1)).output();
		verify(context.event(), times(1)).reason();
		verify(context.event(), times(1)).control();
        verify(context).getMarkerDetails(MARKER_NAME, String.class);
        verify(context).getMarkerDetails(NO_DETAILS_MARKER_NAME, String.class);

		assertThat(arguments).containsExactly(INPUT, OUTPUT, REASON, CONTROL, MARKER_DETAILS, null);
	}

	@Test
	public void defaultParametersCannotBeMultiple() throws Exception {
		// GIVEN a method
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory(dataMapper);
		final Method method = getMethod("defaultParametersCannotBeMultiple");
		// WHEN a ArgumentsProvider is created
		// THEN when used
		assertThatExceptionOfType(IllegalStateException.class)
				.isThrownBy(() -> factory
						.createArgumentsProvider(EventType.ActivityTaskCompleted, method))
				.withMessageContaining("one non-annotated parameter");
	}

	@Test
	public void defaultParameter_dependsOnTheEventHandlerType() throws Exception {
		final SoftAssertions softly = new SoftAssertions();
		assertDefaultArgumentForEventType(softly, EventType.ActivityTaskCompleted, OUTPUT);
		assertDefaultArgumentForEventType(softly, EventType.ActivityTaskFailed, INITIAL_EVENT_ID);
		assertDefaultArgumentForEventType(softly, EventType.ActivityTaskTimedOut, INITIAL_EVENT_ID);
		assertDefaultArgumentForEventType(softly, EventType.WorkflowExecutionStarted, INPUT);
		assertDefaultArgumentForEventType(softly, EventType.WorkflowExecutionSignaled, INPUT);
		assertDefaultArgumentForEventType(softly, EventType.TimerFired, CONTROL);

		assertDefaultArgumentForEventType(softly, EventType.ChildWorkflowExecutionCanceled, DETAILS);
		assertDefaultArgumentForEventType(softly, EventType.ChildWorkflowExecutionCompleted, OUTPUT);
		assertDefaultArgumentForEventType(softly, EventType.ChildWorkflowExecutionFailed, REASON);
		assertDefaultArgumentForEventType(softly, EventType.ChildWorkflowExecutionStarted, RUN_ID);
		assertDefaultArgumentForEventType(softly, EventType.ChildWorkflowExecutionTerminated, INITIAL_EVENT_ID);
		assertDefaultArgumentForEventType(softly, EventType.ChildWorkflowExecutionTimedOut, INITIAL_EVENT_ID);
		assertDefaultArgumentForEventType(softly, EventType.StartChildWorkflowExecutionFailed, CAUSE);
		assertDefaultArgumentForEventType(softly, EventType.WorkflowExecutionCancelRequested, CAUSE);
		softly.assertAll();
	}

	private void assertDefaultArgumentForEventType(final SoftAssertions softly, final EventType eventType,
			final Object expectedArgument) {
		// GIVEN a method
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory(dataMapper);
		final Method method = getMethod("defaultParameter");
		// WHEN a ArgumentsProvider is created
		final EventHandlerArgumentsProvider provider = factory.createArgumentsProvider(eventType, method);
		// THEN when used
		final EventContext context = createMockedContext();
		final Object[] arguments = provider.getArguments(context, this.decisions);
		softly.assertThat(arguments).containsExactly(expectedArgument);
	}

	private EventContext createMockedContext() {
		final WorkflowEvent event = mock(WorkflowEvent.class);
		when(event.cause()).thenReturn(CAUSE);
		when(event.control()).thenReturn(serialize(CONTROL));
		when(event.input()).thenReturn(serialize(INPUT));
		when(event.output()).thenReturn(serialize(OUTPUT));
		when(event.reason()).thenReturn(REASON);
		when(event.details()).thenReturn(DETAILS);
		when(event.initialEventId()).thenReturn(INITIAL_EVENT_ID);
		when(event.runId()).thenReturn(RUN_ID);

        final EventContext context = mock(EventContext.class);
        when(context.event()).thenReturn(event);
        when(context.getMarkerDetails(MARKER_NAME, String.class)).thenReturn(Optional.of(MARKER_DETAILS));
        when(context.getMarkerDetails(eq(NO_DETAILS_MARKER_NAME), any())).thenReturn(Optional.empty());

		return context;
	}

	private Method getMethod(final String methodName) {
		try {
			return Stream.of(Template1.class.getMethods())
					.filter(m -> m.getName().equals(methodName))
					.findFirst().get();
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String serialize(String string) {
		return "\"" + string + "\"";
	}
}
