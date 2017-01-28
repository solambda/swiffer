package com.solambda.swiffer.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

public class Tags {

	private List<String> tags;

	public Tags(final List<String> tags) {
		super();
		Preconditions.checkArgument(tags == null || tags.size() <= 5);
		this.tags = tags;
	}

	public static Tags of(final String... tags) {
		return new Tags(tags != null ? Arrays.asList(tags) : Collections.emptyList());
	}

	public static Tags none() {
		return Tags.of();
	}

	public List<String> get() {
		return tags;
	}
}
