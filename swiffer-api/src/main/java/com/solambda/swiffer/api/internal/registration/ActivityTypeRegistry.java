package com.solambda.swiffer.api.internal.registration;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpleworkflow.AmazonSimpleWorkflow;
import com.amazonaws.services.simpleworkflow.model.ActivityTypeConfiguration;
import com.amazonaws.services.simpleworkflow.model.ActivityTypeDetail;
import com.amazonaws.services.simpleworkflow.model.ActivityTypeInfo;
import com.amazonaws.services.simpleworkflow.model.DescribeActivityTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RegisterActivityTypeRequest;
import com.amazonaws.services.simpleworkflow.model.RegistrationStatus;
import com.amazonaws.services.simpleworkflow.model.TaskList;
import com.amazonaws.services.simpleworkflow.model.TypeAlreadyExistsException;
import com.amazonaws.services.simpleworkflow.model.UnknownResourceException;
import com.google.common.base.Preconditions;
import com.solambda.swiffer.api.ActivityType;
import com.solambda.swiffer.api.internal.SwfAware;

public class ActivityTypeRegistry implements SwfAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityTypeRegistry.class);

	private AmazonSimpleWorkflow swf;
	private String domain;

	public ActivityTypeRegistry(final AmazonSimpleWorkflow swf, final String domain) {
		super();
		this.swf = swf;
		this.domain = domain;
	}

	public boolean isRegistered(final ActivityType activitType) {
		final ActivityTypeDetail detail = getActivityTypeDetail(activitType);
		return detail != null
				&& detail.getTypeInfo() != null
				&& Objects.equals(detail.getTypeInfo().getStatus(), RegistrationStatus.REGISTERED.name());
	}

	private ActivityTypeDetail getActivityTypeDetail(final ActivityType activityType) {
		try {
			final ActivityTypeDetail detail = this.swf.describeActivityType(
					new DescribeActivityTypeRequest()
							.withDomain(this.domain)
							.withActivityType(toActivityType(activityType)));
			return detail;
		} catch (final UnknownResourceException e) {
			return null;
		}
	}

	private com.amazonaws.services.simpleworkflow.model.ActivityType toActivityType(final ActivityType identifier) {
		return new com.amazonaws.services.simpleworkflow.model.ActivityType()
				.withName(identifier.name())
				.withVersion(identifier.version());
	}

	/**
	 * Register the given activity if unregistered, or ensure the registered
	 * activity configuration is the same as the specified one.
	 *
	 * @param activityType
	 * @return true if the activity has been registered by this method, false
	 *         otherwise (already registered)
	 */
	public boolean registerActivityOrCheckConfiguration(final ActivityType activityType) {
		final ActivityTypeDetail detail = getActivityTypeDetail(activityType);
		if (detail != null) {
			LOGGER.debug("Activity '{}' v='{}' is already registered", activityType.name(), activityType.version());
			ensureRegisteredAndSpecifedConfigurationsAreTheSame(detail, activityType);
			return false;
		} else {
			doActivityTypeRegistration(activityType);
			return true;
		}
	}

	private void doActivityTypeRegistration(final ActivityType activityType) {
		try {
			LOGGER.info("Registering activity '{}' v='{}': {}", activityType.name(), activityType.version(),
					activityType);
			this.swf.registerActivityType(new RegisterActivityTypeRequest()
					// identification of the WF
					.withName(activityType.name())
					.withVersion(activityType.version())
					.withDomain(this.domain)
					// description
					.withDescription(activityType.description())

					// defaults behaviors
					.withDefaultTaskHeartbeatTimeout(heartbeat(activityType))
					.withDefaultTaskList(taskList(activityType))
					.withDefaultTaskPriority(taskPriority(activityType))
					.withDefaultTaskScheduleToCloseTimeout(scheduleToClose(activityType))
					.withDefaultTaskScheduleToStartTimeout(scheduleToStart(activityType))
					.withDefaultTaskStartToCloseTimeout(startToClose(activityType)));

		} catch (final TypeAlreadyExistsException e) {
			throw new IllegalStateException("should never occurs", e);
		} catch (final UnknownResourceException e) {
			throw new IllegalStateException(String.format("cannot register the activity %s version:%s in domain %s",
					activityType.name(), activityType.version(), this.domain), e);
		}
	}

	private String heartbeat(final ActivityType type) {
		return negativeToNone(type.defaultTaskHeartbeatTimeout());
	}

	private String scheduleToClose(final ActivityType type) {
		return negativeToNone(type.defaultTaskScheduleToCloseTimeout());
	}

	private String scheduleToStart(final ActivityType type) {
		return negativeToNone(type.defaultTaskScheduleToStartTimeout());
	}

	private String startToClose(final ActivityType type) {
		return negativeToNone(type.defaultTaskStartToCloseTimeout());
	}

	private String negativeToNone(final int timeout) {
		return timeout >= 0 ? Integer.toString(timeout) : "NONE";
	}

	private String taskPriority(final ActivityType activityType) {
		return Integer.toString(activityType.defaultTaskPriority());
	}

	private TaskList taskList(final ActivityType activityType) {
		return new TaskList().withName(activityType.defaultTaskList());
	}

	private void ensureRegisteredAndSpecifedConfigurationsAreTheSame(
			final ActivityTypeDetail registeredDetail,
			final ActivityType activityType) {

		final ActivityTypeDetail specifiedDetail = toActivityTypeDetail(activityType);
		registeredDetail.getTypeInfo()
				.withCreationDate(null)
				.withDeprecationDate(null);
		//
		final String message = String.format(
				"The activity type %s is already registered but with a different configuration than the one specified. "
						+ "Currently registered configuration: %s. "
						+ "Specified configuration: %s",
				activityType, registeredDetail, specifiedDetail);
		Preconditions.checkState(Objects.equals(specifiedDetail, registeredDetail), message);
	}

	private ActivityTypeDetail toActivityTypeDetail(final ActivityType activityType) {
		final ActivityTypeInfo specifiedInfo = new ActivityTypeInfo()
				.withDescription(activityType.description())
				.withStatus(RegistrationStatus.REGISTERED)
				.withActivityType(toActivityType(activityType));
		final ActivityTypeConfiguration specifiedConfiguration = new ActivityTypeConfiguration()
				.withDefaultTaskHeartbeatTimeout(heartbeat(activityType))
				.withDefaultTaskScheduleToCloseTimeout(scheduleToClose(activityType))
				.withDefaultTaskScheduleToStartTimeout(scheduleToStart(activityType))
				.withDefaultTaskList(taskList(activityType))
				.withDefaultTaskPriority(taskPriority(activityType))
				.withDefaultTaskStartToCloseTimeout(startToClose(activityType));
		return new ActivityTypeDetail()
				.withConfiguration(specifiedConfiguration)
				.withTypeInfo(specifiedInfo);
	}

	@Override
	public AmazonSimpleWorkflow swf() {
		return this.swf;
	}

	@Override
	public String domain() {
		return this.domain;
	}

}
