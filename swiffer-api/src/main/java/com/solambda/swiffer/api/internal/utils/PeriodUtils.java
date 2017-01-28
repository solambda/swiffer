package com.solambda.swiffer.api.internal.utils;

import java.time.Period;

public class PeriodUtils {

	public static Period toDays(final String days) {
		if (days.equals("NONE")) {
			return Period.ZERO;
		}
		return Period.ofDays(Integer.parseInt(days));
	}
}
