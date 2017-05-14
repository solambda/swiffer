package com.solambda.swiffer.api.internal.utils;

import java.util.Arrays;

import com.amazonaws.services.simpleworkflow.model.WorkflowType;
import com.google.common.base.Preconditions;

public class SWFUtils {

	/**
	 * Ensure the specified string :
	 * <ul>
	 * <li>is not null
	 * <li>is between 1 and 256 chars long
	 * <li>does not start or end with whitespace.
	 * <li>does not contain a : (colon), / (slash), | (vertical bar), or any
	 * control characters (\u0000-\u001f | \u007f - \u009f).
	 * <li>does not contain the literal string "arn"
	 * </ul>
	 *
	 * @param id
	 * @return the id
	 */
	public static String checkId(final String id) {
		checkIdLength(id);
		Preconditions.checkArgument(id.trim().equals(id),
				"must not start or end with whitespace");
		Preconditions.checkArgument(!id.matches(".*([:/|]|\\p{Cntrl}).*"),
				"must not contain a : (colon), / (slash), | (vertical bar), or any control characters (\\u0000-\\u001f | \\u007f - \\u009f).");
		Preconditions.checkArgument(!id.contains("arn"), "must not contain the literal string 'arn'");
		return id;
	}

	/**
	 * Ensure the specified string :
	 * <ul>
	 * <li>is not null
	 * <li>is between 1 and 256 chars long
	 * </ul>
	 *
	 * @param id
	 * @return the id
	 */
	public static String checkIdLength(final String id) {
		Preconditions.checkArgument(id != null, "cannot be null");
		Preconditions.checkArgument(id.length() >= 1 && id.length() <= 256,
				"must be between 1 and 256 characters");
		return id;
	}

	public static String checkVersion(final String version) {
		Preconditions.checkArgument(version != null, "cannot be null");
		Preconditions.checkArgument(version.length() >= 1 && version.length() <= 64,
				"must be between 1 and 64 characters");
		Preconditions.checkArgument(version.trim().equals(version),
				"must not start or end with whitespace");
		Preconditions.checkArgument(!version.matches(".*([:/|]|\\p{Cntrl}).*"),
				"must not contain a : (colon), / (slash), | (vertical bar), or any control characters (\\u0000-\\u001f | \\u007f - \\u009f).");
		Preconditions.checkArgument(!version.contains("arn"), "must not contain the literal string 'arn'");
		return version;
	}

	/**
	 * Converts class with {@link com.solambda.swiffer.api.WorkflowType} annotation to the {@link WorkflowType}
	 *
	 * @param workflowType class, annotated with {@link com.solambda.swiffer.api.WorkflowType}
	 * @return {@link WorkflowType} used by amazon's SWF
	 */
	public static WorkflowType toSWFWorkflowType(Class<?> workflowType) {
		com.solambda.swiffer.api.WorkflowType annotation = workflowType.getAnnotation(com.solambda.swiffer.api.WorkflowType.class);
		return new WorkflowType().withName(annotation.name()).withVersion(annotation.version());
	}

	/**
	 * Returns a default value if the object passed is {@code null}.
	 *
	 * @param <T>          the type of the object
	 * @param object       the {@code Object} to test, may be {@code null}
	 * @param defaultValue the default value to return, may be {@code null}
	 * @return {@code object} if it is not {@code null}, defaultValue otherwise
	 */
	public static <T> T defaultIfNull(T object, T defaultValue) {
		return object != null ? object : defaultValue;
	}

	/**
	 * Check if a given {@code string} starts with any of an array of specified {@code searchStrings}.
	 *
	 * @param string        the String to check, may be null
	 * @param searchStrings the Strings to find, may be null or empty
	 * @return {@code true} if the {@code string} starts with any of the the prefixes
	 */
	public static boolean startsWithAny(String string, String... searchStrings) {
		if (string == null || searchStrings == null) {
			return false;
		}

		return Arrays.stream(searchStrings).anyMatch(string::startsWith);
	}
}
