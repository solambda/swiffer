package com.solambda.aws.swiffer.api.model.decider.context.identifier;

import com.solambda.aws.swiffer.api.model.WorkflowTypeId;

public class WorkflowName implements ContextName {

	private WorkflowTypeId type;

	public WorkflowName(final WorkflowTypeId type) {
		super();
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		WorkflowName other = (WorkflowName) obj;
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "WorkflowName[" + type + "]";
	}

}
