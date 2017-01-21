package com.solambda.swiffer.api.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tags {

	private List<String> tags;

	public Tags(final List<String> tags) {
		super();
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
