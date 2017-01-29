package com.solambda.swiffer.api.internal.decisions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.api.WorkflowType;

public class WorkflowTemplateFactoryTest {

	@WorkflowType(name = "workflowType1", version = "1")
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface WorkflowDef1 {

	}

	@WorkflowDef1
	public static class WorkflowTemplate1 {
		@OnWorkflowStarted
		public void started(final String input) {

		}
	}

	@Test
	public void createWorkflowType_returnsACorrectVersionedName() throws Exception {
		final WorkflowTemplateFactory factory = new WorkflowTemplateFactory();
		// final VersionedName result = factory.createWorkflowType(new
		// WorkflowTemplate1());
		// assertThat(result.name()).isEqualTo("workflowType1");
		// assertThat(result.version()).isEqualTo("1");
	}

	@Test
	public void createWorkflowTemplate() {
		final WorkflowTemplateFactory factory = new WorkflowTemplateFactory();
		final WorkflowTemplate template = factory.createWorkflowTemplate(new WorkflowTemplate1());
	}

}
