package com.solambda.swiffer.api.internal.utils;

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
}
