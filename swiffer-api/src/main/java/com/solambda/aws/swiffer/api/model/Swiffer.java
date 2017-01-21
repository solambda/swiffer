package com.solambda.aws.swiffer.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.solambda.aws.swiffer.api.model.decider.DeciderConfigurer;
import com.solambda.aws.swiffer.api.model.decider.DeciderService;
import com.solambda.aws.swiffer.api.model.decider.WorkflowTypeRegistryDelegatorDecider;
import com.solambda.aws.swiffer.api.model.decider.impl.DeciderServiceImpl;

public class Swiffer {

	private AmazonSimpleWorkflow swf;
	private String domain;
	private SwifferDecider swifferDecider;

	protected Swiffer(final AmazonSimpleWorkflow swf, final String domain) {
		super();
		this.swf = swf;
		this.domain = domain;
	}

	public static Swiffer get(final AmazonSimpleWorkflow swf, final String domain) {
		return new Swiffer(swf, domain);
	}

	public class SwifferDecider {
		private List<DeciderConfigurer> configurers = new ArrayList<DeciderConfigurer>();
		private DeciderService service;

		public SwifferDecider() {
			super();
		}

		public SwifferDecider add(final DeciderConfigurer configurer) {
			configurers.add(configurer);
			return this;
		}

		public DeciderService start() {
			DeciderService service = get();
			service.start();
			return service;
		}

		public DeciderService get() {
			if (service == null) {
				service = createDeciderService();
			}
			return service;
		}

		private DeciderService createDeciderService() {
			WorkflowTypeRegistryDelegatorDecider decider = new WorkflowTypeRegistryDelegatorDecider(configurers);
			DeciderService service = new DeciderServiceImpl(swf, domain, "default", "default-swiffer-decision-poller", 1, decider);
			return service;
		}

	}

	public class SwifferTaskExecutor {
	}

	public SwifferDecider decider(final DeciderConfigurer configurer) {
		return swifferDecider().add(configurer);
	}

	private SwifferDecider swifferDecider() {
		if (swifferDecider == null) {
			swifferDecider = new SwifferDecider();
		}
		return swifferDecider;
	}

	public SwifferTaskExecutor task(final TaskType taskType) {
		SwifferTaskExecutor executor = new SwifferTaskExecutor();

		return executor;
	}

	public void task(final TaskType sleepTask, final Consumer<String> c) {
	}
}
