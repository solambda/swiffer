package com.solambda.swiffer.api.internal.decisions;

import static org.mockito.Mockito.mock;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import com.solambda.swiffer.api.OnWorkflowStarted;
import com.solambda.swiffer.api.WorkflowType;
import com.solambda.swiffer.api.duration.DefaultDurationTransformer;
import com.solambda.swiffer.api.duration.DurationTransformer;
import com.solambda.swiffer.api.mapper.DataMapper;
import com.solambda.swiffer.api.mapper.JacksonDataMapper;
import com.solambda.swiffer.api.retry.RetryPolicy;

public class WorkflowTemplateFactoryTest {
	private final DataMapper dataMapper = new JacksonDataMapper();
	private final DurationTransformer durationTransformer = new DefaultDurationTransformer();
    private final RetryPolicy globalRetryPolicy = mock(RetryPolicy.class);

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
		final WorkflowTemplateFactory factory = new WorkflowTemplateFactory(dataMapper, durationTransformer, globalRetryPolicy);
		// final VersionedName result = factory.createWorkflowType(new
		// WorkflowTemplate1());
		// assertThat(result.name()).isEqualTo("workflowType1");
		// assertThat(result.version()).isEqualTo("1");
	}

	@Test
	public void createWorkflowTemplate() {
		final WorkflowTemplateFactory factory = new WorkflowTemplateFactory(dataMapper, durationTransformer, globalRetryPolicy);
		final WorkflowTemplate template = factory.createWorkflowTemplate(new WorkflowTemplate1());
	}

}
