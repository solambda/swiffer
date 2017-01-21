package com.solambda.swiffer.api.model.decider.context.identifier;

public class TimerName implements ContextName {

	private String name;

	public TimerName(final String name) {
		super();
		this.name = name;
	}

	public static TimerName of(final String name) {
		return new TimerName(name);
	};

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
		TimerName other = (TimerName) obj;
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
		return "TimerName[" + name + "]";
	}

	public String name() {
		return name;
	}

}
