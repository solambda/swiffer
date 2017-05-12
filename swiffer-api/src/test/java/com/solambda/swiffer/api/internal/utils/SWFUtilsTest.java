package com.solambda.swiffer.api.internal.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SWFUtilsTest {

    @Test
    public void startsWithAny() throws Exception {
        assertThat(SWFUtils.startsWithAny("tes12345", "te", "tes", "test")).isTrue();
    }

    @Test
    public void startsWithAny_NoPrefixesToTest() throws Exception {
        assertThat(SWFUtils.startsWithAny("tes12345")).isFalse();
    }

    @Test
    public void startsWithAny_NothingToTest() throws Exception {
        assertThat(SWFUtils.startsWithAny(null)).isFalse();
    }

    @Test
    public void startsWithAny_NoTargetToTest() throws Exception {
        assertThat(SWFUtils.startsWithAny(null, "te", "tes", "test")).isFalse();
    }

    @Test
    public void startsWithAny_EmptyPrefixesToTest() throws Exception {
        assertThat(SWFUtils.startsWithAny(null, new String[]{})).isFalse();
    }
}