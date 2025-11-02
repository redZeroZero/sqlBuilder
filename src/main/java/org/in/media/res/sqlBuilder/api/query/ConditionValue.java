package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;
import java.util.Objects;

import org.in.media.res.sqlBuilder.constants.ValueType;

public final class ConditionValue {
	private final Object value;
	private final ValueType type;

	private ConditionValue(Object value, ValueType type) {
		this.value = value;
		this.type = type;
	}

	public static ConditionValue of(String value) {
		return new ConditionValue(Objects.requireNonNull(value, "value"), ValueType.TY_STR);
	}

	public static ConditionValue of(Integer value) {
		return new ConditionValue(Objects.requireNonNull(value, "value"), ValueType.TY_INT);
	}

	public static ConditionValue of(Double value) {
		return new ConditionValue(Objects.requireNonNull(value, "value"), ValueType.TY_DBL);
	}

	public static ConditionValue of(Date value) {
		return new ConditionValue(Objects.requireNonNull(value, "value"), ValueType.TY_DATE);
	}

	public Object value() {
		return value;
	}

	public ValueType type() {
		return type;
	}
}
