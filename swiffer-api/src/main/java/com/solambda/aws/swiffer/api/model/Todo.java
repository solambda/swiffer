package com.solambda.aws.swiffer.api.model;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;

public class Todo {

	private static void test(final AmazonSimpleWorkflow w) {

		// EXTERNALLY (ADMIN)
		// w.listActivityTypes(listActivityTypesRequest)
		// w.listDomains(listDomainsRequest)
		// w.listWorkflowTypes(listWorkflowTypesRequest)

		// w.getWorkflowExecutionHistory(getWorkflowExecutionHistoryRequest)

		// w.countClosedWorkflowExecutions(countClosedWorkflowExecutionsRequest);
		// w.countOpenWorkflowExecutions(countOpenWorkflowExecutionsRequest)
		// w.countPendingActivityTasks(countPendingActivityTasksRequest)
		// w.countPendingDecisionTasks(countPendingDecisionTasksRequest)

		// REGISTRY / CONFIGURATON
		// w.registerDomain(registerDomainRequest); //name,
		// metadata(desc,retention)
		// w.deprecateDomain(deprecateDomainRequest);//name
		// w.describeDomain(describeDomainRequest)//name

		// w.registerWorkflowType(registerWorkflowTypeRequest);//name+version,
		// metadata()
		// w.deprecateWorkflowType(deprecateWorkflowTypeRequest);//
		// w.describeWorkflowType(describeWorkflowTypeRequest)

		// w.registerActivityType(registerActivityTypeRequest);
		// w.deprecateActivityType(deprecateActivityTypeRequest);
		// w.describeActivityType(describeActivityTypeRequest)

		// TECHNICAL
		// w.getCachedResponseMetadata(request)
		// w.setEndpoint(endpoint);
		// w.setRegion(region);
		// w.shutdown();
	}
}
