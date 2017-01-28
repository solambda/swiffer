package com.solambda.swiffer.api.internal.decisions;

import com.amazonaws.services.simpleworkflow.model.EventType;
import com.solambda.swiffer.api.internal.context.identifier.ContextName;

public class EventHandlerType {

	private EventType eventType;
	private ContextName contextName;

	public EventHandlerType(final EventType eventType, final ContextName contextName) {
		super();
		this.eventType = eventType;
		this.contextName = contextName;
	}

	public EventType getEventType() {
		return this.eventType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.contextName == null ? 0 : this.contextName.hashCode());
		result = prime * result + (this.eventType == null ? 0 : this.eventType.hashCode());
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
		final EventHandlerType other = (EventHandlerType) obj;
		if (this.contextName == null) {
			if (other.contextName != null) {
				return false;
			}
		} else if (!this.contextName.equals(other.contextName)) {
			return false;
		}
		if (this.eventType == null) {
			if (other.eventType != null) {
				return false;
			}
		} else if (!this.eventType.equals(other.eventType)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Type=" + this.eventType + ",Name=" + this.contextName + "]";
	}
}
