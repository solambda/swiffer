package com.solambda.aws.swiffer.annotation.basic;

import com.solambda.aws.swiffer.api.model.decider.DecisionContext;
import com.solambda.aws.swiffer.api.model.tasks.Task;
import com.solambda.aws.swiffer.integration.APITest2.DecisionMaker;

//TODO show how the decider should be expressed
public class BasicAnnotationBasedDeciderExample {

	@WorkflowStarted public void()
	{

	}

	@TaskCompleted(taskName="fezaf", taskVersion="greker")
	public void onTaskCompleted(@TaskOutput final MyTaskOutput o, @State final MyState state, final DecisionContext context, final Decisions decisions){
		//you may use here:
		//- the task output (which is optional), and can be got as a String or as a strong type (in case the DeserializerStrategy is used
		//- the state of the Workflow, which is readfrom the Workflow hsiteory
		//- the decisioncontext to get more control
		//- the decisiosn to take
		decisions.
	}

}
