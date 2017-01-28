package com.solambda.swiffer.api.internal.context.identifier;

import com.solambda.swiffer.api.internal.VersionedName;

public class TaskName implements ContextName {

	private VersionedName type;

	public TaskName(final VersionedName type) {
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
		TaskName other = (TaskName) obj;
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
		return "TaskName[" + type + "]";
	}

}
