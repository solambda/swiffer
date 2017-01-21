package com.solambda.aws.swiffer.api.model.decider.context.identifier;

public class MarkerName implements ContextName {

	private String name;

	public MarkerName(final String name) {
		super();
		this.name = name;
	}

	public static MarkerName of(final String name) {
		return new MarkerName(name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (name == null ? 0 : name.hashCode());
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
		MarkerName other = (MarkerName) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MarkerName[" + name + "]";
	}

}
