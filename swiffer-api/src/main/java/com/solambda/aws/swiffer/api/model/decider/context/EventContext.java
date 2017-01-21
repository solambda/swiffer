package com.solambda.aws.swiffer.api.model.decider.context;

import com.solambda.aws.swiffer.api.model.ContextCategory;
import com.solambda.aws.swiffer.api.model.ContextType;
import com.solambda.aws.swiffer.api.model.decider.DecisionContext;
import com.solambda.aws.swiffer.api.model.decider.context.identifier.ContextName;

public interface EventContext extends DecisionContext {

	// should be on the event context level
	/**
	 * @return the category the context belongs to
	 */
	ContextCategory category();

	/**
	 * @return the name of the context (signal name, task type, workflow type,
	 *         timer name, etc.)
	 */
	ContextName name();

	ContextType contextType();

}
