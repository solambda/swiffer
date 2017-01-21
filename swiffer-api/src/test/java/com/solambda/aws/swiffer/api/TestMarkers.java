package com.solambda.aws.swiffer.api;

import org.junit.Test;

import com.solambda.aws.swiffer.api.model.Workflow;
import com.solambda.aws.swiffer.api.model.WorkflowBuilder;
import com.solambda.aws.swiffer.api.model.decider.*;
import com.solambda.aws.swiffer.api.model.decider.impl.DecisionContextProviderImpl;
import com.solambda.aws.swiffer.api.test.ObjectMother;
import com.solambda.aws.swiffer.api.test.WorkflowConsumerTest;

public class TestMarkers {

	private EventContextHandlerRegistry registry = new EventContextHandlerRegistry();
	private EventDelegatorDecider delegator = new EventDelegatorDecider(registry);

	private Workflow workflow;

	private ContextProvider<DecisionContext> provider = new DecisionContextProviderImpl(ObjectMother.client(), ObjectMother.domainName(), null,
			TestEventDelegatorDeciderIsNotified.class.getName());
	private WorkflowConsumerTest consumer = new WorkflowConsumerTest(delegator, provider);

	private WorkflowBuilder builder = new WorkflowBuilder()
			.client(ObjectMother.client())
			.domain(ObjectMother.domainName())
			.type(ObjectMother.registeredWorkflowType())
			.id("TestEventDelegatorDeciderIsNotified");

	@Test
	public void severalMarkersWithTheSameNameCanBeCreated() throws Exception {
		throw new IllegalStateException("not implemented");
	}

	@Test
	public void aMarkerCanBeRetrievedInTheHistory() throws Exception {
		// when started, schedule task and record marker
		// when
		throw new IllegalStateException("not implemented");
	}
}
