package com.solambda.swiffer.api.internal.decisions;

import java.lang.annotation.Annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.WorkflowType;
import com.solambda.swiffer.api.internal.VersionedName;
import com.solambda.swiffer.api.mapper.DataMapper;

public class WorkflowTemplateFactory {

	public static final Logger LOGGER = LoggerFactory.getLogger(WorkflowTemplateFactory.class);

	private final DataMapper dataMapper;

	public WorkflowTemplateFactory(DataMapper dataMapper) {
		this.dataMapper = dataMapper;
	}

	/**
	 * @param template
	 *            create a {@link WorkflowTemplate} instance by introspecting a
	 *            user defined template containing annotation based event
	 *            handlers.
	 *            <p>
	 * @return
	 */
	public WorkflowTemplate createWorkflowTemplate(final Object template) {
		final VersionedName workflowType = createWorkflowType(template);
		LOGGER.debug("WorkflowType found: name={}, version={}", workflowType.name(), workflowType.version());
		final EventHandlerRegistryFactory builder = new EventHandlerRegistryFactory(workflowType, dataMapper);
		final EventHandlerRegistry eventHandlerRegistry = builder.build(template);
		return new WorkflowTemplateImpl(workflowType, eventHandlerRegistry, dataMapper);
	}

	private VersionedName createWorkflowType(final Object template) {
		final WorkflowType workflowType = findWorkflowTypeAnnotation(template);
		return new VersionedName(workflowType.name(), workflowType.version());

	}

	public static WorkflowType findWorkflowTypeAnnotation(final Object workflowTemplate) {
		final Class<?> clazz = workflowTemplate.getClass();
		LOGGER.debug("Search workflow type information for {}", clazz);
		final WorkflowType workflowType = findWorkflowType(clazz);
		Preconditions.checkState(workflowType != null,
				"The provided object %s has no workflow type information. "
						+ "Annotate it with a custom annotation which is itself"
						+ " annotated with %s. Set the annotation Retention to SOURCE level",
				clazz.getName(), WorkflowType.class.getName());
		return workflowType;
	}

	private static WorkflowType findWorkflowType(final Class<?> clazz) {
		final Annotation[] annotations = clazz.getAnnotations();
		for (final Annotation annotation : annotations) {
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			return annotationType.getAnnotation(WorkflowType.class);
		}
		return null;
	}

}
