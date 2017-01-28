package com.solambda.swiffer.api.test;

public class WorkflowConsumerTest {
	//
	// private static Logger LOGGER =
	// LoggerFactory.getLogger(WorkflowConsumerTest.class);
	// private final ExecutorService executor =
	// Executors.newFixedThreadPool(10);
	//
	// private ContextProvider<DecisionTaskContext> contextProvider;
	// private Decider decider;
	//
	// public WorkflowConsumerTest(final Decider decider, final
	// ContextProvider<DecisionTaskContext> provider) {
	// super();
	// this.contextProvider = provider;
	// this.decider = decider;
	// }
	//
	// public void consume() {
	// // should eimt an asynchronous task, timeout to accelerate error cases
	//
	// DecisionTaskContext context = getContextTimeout(Duration.ofSeconds(5));
	// if (context != null) {
	// try {
	// DecisionsImpl decisions = new DecisionsImpl();
	// decider.makeDecisions(context, decisions);
	// DecisionExecutor applier = new
	// DecisionExecutorImpl(ObjectMother.client());
	// applier.apply(context, decisions);
	// } catch (Exception e) {
	// fail("error during workflow decisions", e);
	// }
	// } else {
	// fail("impossible to consume decision task: there is not !");
	// }
	// }
	//
	// private DecisionTaskContext getContextTimeout(final Duration ofSeconds) {
	// Future<DecisionTaskContext> context = executor.submit(() ->
	// contextProvider.get());
	// try {
	// return context.get(ofSeconds.getSeconds(), TimeUnit.SECONDS);
	// } catch (TimeoutException e) {
	// return null;
	// } catch (Exception e) {
	// throw new IllegalStateException("cannot poll for decision task", e);
	// }
	// }
}
