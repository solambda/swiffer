package com.solambda.swiffer.api.exceptions;

/**
 * Exception raised during creation of a Workflow Template.
 */
public class WorkflowTemplateException extends RuntimeException {

	public WorkflowTemplateException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public WorkflowTemplateException(final String message) {
		super(message);
	}

}
