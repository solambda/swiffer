package com.solambda.aws.swiffer.api.model.decider;

import java.util.HashMap;
import java.util.Map;

import com.solambda.aws.swiffer.api.model.ContextType;
import com.solambda.aws.swiffer.api.model.decider.context.EventContext;
import com.solambda.aws.swiffer.api.model.decider.context.WorkflowStartedContext;
import com.solambda.aws.swiffer.api.model.decider.context.identifier.ContextName;
import com.solambda.aws.swiffer.api.model.decider.handler.EventContextHandler;
import com.solambda.aws.swiffer.api.model.decider.handler.WorkflowStartedHandler;

public class EventContextHandlerRegistry {

	private Map<Key, EventContextHandler<?>> registry = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <C extends EventContext, H extends EventContextHandler<C>> H get(final C context) {
		Key key = new Key(context.contextType(), context.name());
		return (H) registry.get(key);
	}

	public WorkflowStartedHandler getWorkflowStarted(final WorkflowStartedContext context) {
		return (WorkflowStartedHandler) registry.get(new Key(context.contextType(), context.name()));
	}

	public <H extends EventContextHandler<C>, C extends EventContext> void register(final ContextName type, final Class<C> contextType,
			final H handler) {
		Key key = new Key(new ContextType(contextType), type);
		if (registry.containsKey(key)) {
			throw new IllegalStateException("already contains " + key);
		}
		registry.put(key, handler);
	}

	private static class Key {
		private ContextType contextType;
		private ContextName contextName;

		public Key(final ContextType contextType, final ContextName contextName) {
			super();
			this.contextType = contextType;
			this.contextName = contextName;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (contextName == null ? 0 : contextName.hashCode());
			result = prime * result + (contextType == null ? 0 : contextType.hashCode());
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
			Key other = (Key) obj;
			if (contextName == null) {
				if (other.contextName != null) {
					return false;
				}
			} else if (!contextName.equals(other.contextName)) {
				return false;
			}
			if (contextType == null) {
				if (other.contextType != null) {
					return false;
				}
			} else if (!contextType.equals(other.contextType)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "Type=" + contextType + ",Name=" + contextName + "]";
		}

	}

}
