package org.in.media.res.sqlBuilder.core.query.predicate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.constants.Operator;

/**
 * Utility for converting arbitrary value overloads into {@link ConditionValue}
 * lists while normalizing operators (e.g. EQ + multiple values becomes IN).
 */
public final class PredicateValues {

	private PredicateValues() {
	}

	public static Result strings(Operator operator, String... values) {
		return map(operator, Objects.requireNonNull(values, "values"), ConditionValue::of);
	}

	public static Result integers(Operator operator, Integer... values) {
		return map(operator, Objects.requireNonNull(values, "values"), ConditionValue::of);
	}

	public static Result doubles(Operator operator, Double... values) {
		return map(operator, Objects.requireNonNull(values, "values"), ConditionValue::of);
	}

	public static Result dates(Operator operator, Date... values) {
		return map(operator, Objects.requireNonNull(values, "values"), ConditionValue::of);
	}

	public static Result numbers(Operator operator, Number... values) {
		Objects.requireNonNull(values, "values");
		if (values.length == 0) {
			throw missingValues(operator);
		}
		List<ConditionValue> mapped = new ArrayList<>(values.length);
		for (Number value : values) {
			Objects.requireNonNull(value, "value");
			if (value instanceof Double || value instanceof Float) {
				mapped.add(ConditionValue.of(value.doubleValue()));
			} else {
				mapped.add(ConditionValue.of(value.intValue()));
			}
		}
		return new Result(normalize(operator, mapped.size()), List.copyOf(mapped));
	}

	private static <T> Result map(Operator operator, T[] values, Function<T, ConditionValue> mapper) {
		if (values.length == 0) {
			throw missingValues(operator);
		}
		List<ConditionValue> mapped = new ArrayList<>(values.length);
		for (T value : values) {
			mapped.add(mapper.apply(Objects.requireNonNull(value, "value")));
		}
		return new Result(normalize(operator, mapped.size()), List.copyOf(mapped));
	}

	private static Operator normalize(Operator operator, int valueCount) {
		if (operator == Operator.EQ && valueCount > 1) {
			return Operator.IN;
		}
		return operator;
	}

	private static IllegalArgumentException missingValues(Operator operator) {
		String label = operator != null ? operator.name() : "condition";
		return new IllegalArgumentException(label + " requires at least one value");
	}

	public record Result(Operator operator, List<ConditionValue> values) {
	}
}
