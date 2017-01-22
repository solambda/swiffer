package com.solambda.swiffer.api.internal;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.base.Strings;
import com.solambda.swiffer.api.model.TaskListIdentifier;

public class TaskListIdentifierTest {

	@Test
	public void mustNotBeNull() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier(null));
	}

	@Test
	public void mustNotBeEmpty() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier(""));
	}

	@Test
	public void mustNotBeMoreThan256() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier(Strings.repeat("a", 257)));
	}

	@Test
	public void mustNotStartOrEndWithWhitespace() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier(" blablah"));
	}

	@Test
	public void mustNotContainTheLiteralStringArn() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier("blaharnblah"));
	}

	@Test
	public void mustNotContainControlCharacters() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier("blah\u001fblah"));
	}

	@Test
	public void mustNotContainColon() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier("blah:blah"));
	}

	@Test
	public void mustNotContainSlash() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier("blah/blah"));
	}

	@Test
	public void mustNotContainPipe() throws Exception {
		Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> new TaskListIdentifier("blah|blah"));
	}

}
