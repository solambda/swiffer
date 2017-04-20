package com.solambda.swiffer.api.internal.context.identifier;

import com.solambda.swiffer.api.internal.VersionedName;

public class WorkflowName implements ContextName {

	private VersionedName type;

	public WorkflowName(final VersionedName type) {
		super();
		this.type = type;
	}

	public WorkflowName(String name, String version) {
		type = new VersionedName(name, version);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.type == null ? 0 : this.type.hashCode());
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
		final WorkflowName other = (WorkflowName) obj;
		if (this.type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!this.type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "WorkflowName[" + this.type + "]";
	}

}
