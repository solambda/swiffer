package com.solambda.aws.swiffer.api.model.decider;

public interface DeciderService {
	public void start();

	public void stop();

	public void awaitStopped();
}
