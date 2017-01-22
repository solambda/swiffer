package com.solambda.swiffer.api;

import java.util.Arrays;
import java.util.List;

import com.solambda.swiffer.api.internal.DeciderImpl;

/**
 * A builder of {@link Decider}.
 */
public class DeciderBuilder {

	private String decisionTaskList;
	private String activityTaskList;
	private String identity;
	private List<Object> workflowTemplates;

	/**
	 * @return a new instance of {@link Decider}
	 */
	public Decider build() {
		return new DeciderImpl();
	}

	/**
	 * @param decisionTaskList
	 *            the task list to poll for decision tasks
	 * @return this builder
	 */
	public DeciderBuilder decisionTaskList(final String decisionTaskList) {
		this.decisionTaskList = decisionTaskList;
		return this;
	}

	/**
	 *
	 * @param activityTaskList
	 *            the default activity task list to schedule activity in
	 * @return this builder
	 */
	public DeciderBuilder activityTaskList(final String activityTaskList) {
		this.activityTaskList = activityTaskList;
		return this;
	}

	/**
	 * Optional name of the decider
	 *
	 * @param identity
	 *            name of the Decider, for information
	 * @return this builder
	 */
	public DeciderBuilder identity(final String identity) {
		this.identity = identity;
		return this;
	}

	/**
	 * Required
	 *
	 * @param workflowTemplates
	 *            the templates that should handle business logic of polled
	 *            decision tasks
	 * @return this builder
	 */
	public DeciderBuilder workflowTemplates(final Object... workflowTemplates) {
		this.workflowTemplates = Arrays.asList(workflowTemplates);
		return this;
	}
}
