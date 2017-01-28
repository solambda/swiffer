package com.solambda.swiffer.api.internal.context.identifier;

public class SignalName implements ContextName {

	private String name;

	public SignalName(final String name) {
		super();
		this.name = name;
	}

	public static SignalName of(final String name) {
		return new SignalName(name);
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
		SignalName other = (SignalName) obj;
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
		return "SignalName[" + name + "]";
	}

	public String name() {
		return name;
	}

}
