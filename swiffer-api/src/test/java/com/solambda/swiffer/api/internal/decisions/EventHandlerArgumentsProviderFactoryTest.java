package com.solambda.swiffer.api.internal.decisions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.Control;
import com.solambda.swiffer.api.Decisions;
import com.solambda.swiffer.api.Input;
import com.solambda.swiffer.api.Output;
import com.solambda.swiffer.api.Reason;

public class EventHandlerArgumentsProviderFactoryTest {
	private static final EventType ANY_TYPE = null;
	private static final String CAUSE = "cause";
	private static final String CONTROL = "control";
	private static final String INPUT = "input";
	private static final String OUTPUT = "output";
	private static final String REASON = "reason";
	private static final String DETAILS = "details";

	private final Decisions decisions = new DecisionsImpl();

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
		 * used to assert the default paramters cannot be multiple
		 *
		 */
		public void defaultParametersCannotBeMultiple(final String input, final String output) {

		}

		public void specificAnnotations(
				final @Input String input,
				final @Output String output,
				final @Reason String reason,
				final @Control String control) {

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
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory();
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
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory();
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
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory();
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
		assertThat(arguments).containsExactly(INPUT, OUTPUT, REASON, CONTROL);
	}

	@Test
	public void defaultParametersCannotBeMultiple() throws Exception {
		// GIVEN a method
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory();
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
		assertDefaultArgumentForEventType(softly, EventType.ActivityTaskFailed, REASON);
		assertDefaultArgumentForEventType(softly, EventType.WorkflowExecutionStarted, INPUT);
		assertDefaultArgumentForEventType(softly, EventType.WorkflowExecutionSignaled, INPUT);
		assertDefaultArgumentForEventType(softly, EventType.TimerFired, CONTROL);
		softly.assertAll();
	}

	private void assertDefaultArgumentForEventType(final SoftAssertions softly, final EventType eventType,
			final Object expectedArgument) {
		// GIVEN a method
		final EventHandlerArgumentsProviderFactory factory = new EventHandlerArgumentsProviderFactory();
		final Method method = getMethod("defaultParameter");
		// WHEN a ArgumentsProvider is created
		final EventHandlerArgumentsProvider provider = factory.createArgumentsProvider(eventType, method);
		// THEN when used
		final EventContext context = createMockedContext();
		final Object[] arguments = provider.getArguments(context, this.decisions);
		softly.assertThat(arguments).containsExactly(expectedArgument);
	}

	private EventContext createMockedContext() {
		final EventContext context = mock(EventContext.class);
		final WorkflowEvent event = mock(WorkflowEvent.class);
		when(context.event()).thenReturn(event);
		when(event.cause()).thenReturn(CAUSE);
		when(event.control()).thenReturn(CONTROL);
		when(event.input()).thenReturn(INPUT);
		when(event.output()).thenReturn(OUTPUT);
		when(event.reason()).thenReturn(REASON);
		when(event.details()).thenReturn(DETAILS);
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
}
