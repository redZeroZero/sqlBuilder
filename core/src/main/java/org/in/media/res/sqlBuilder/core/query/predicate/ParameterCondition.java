package org.in.media.res.sqlBuilder.core.query.predicate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;

/**
 * {@link Condition} implementation that renders a bare parameter check such as {@code ? IS NULL}.
 * Used to express optional filters that guard predicates with {@code (param IS NULL OR ...)} logic.
 */
public final class ParameterCondition implements Condition {

	private final Operator startOperator;
	private final SqlParameter<?> parameter;

	private ParameterCondition(Operator startOperator, SqlParameter<?> parameter) {
		this.startOperator = startOperator;
		this.parameter = Objects.requireNonNull(parameter, "parameter");
	}

	public static ParameterCondition isNull(SqlParameter<?> parameter) {
		return new ParameterCondition(null, parameter);
	}

	public ParameterCondition withStartOperator(Operator operator) {
		if (Objects.equals(this.startOperator, operator)) {
			return this;
		}
		return new ParameterCondition(operator, this.parameter);
	}

	@Override
	public List<ConditionValue> values() {
		return List.of(ConditionValue.of(parameter));
	}

	@Override
	public Column getLeft() {
		return null;
	}

	@Override
	public Column getRight() {
		return null;
	}

	@Override
	public Operator getStartOperator() {
		return startOperator;
	}

	@Override
	public Operator getOperator() {
		return Operator.IS_NULL;
	}

	@Override
	public AggregateOperator getLeftAgg() {
		return null;
	}

	@Override
	public AggregateOperator getRightAgg() {
		return null;
	}

	@Override
	public String transpile() {
		String cast = sqlType(parameter.type());
		StringBuilder builder = new StringBuilder();
		if (startOperator != null) {
			builder.append(startOperator.value());
		}
		builder.append(cast != null ? "CAST(? AS " + cast + ")" : "?")
				.append(' ')
				.append(Operator.IS_NULL.value().trim());
		return builder.toString();
	}

	private static String sqlType(Class<?> type) {
		if (type == null || Object.class.equals(type)) {
			return null;
		}
		if (String.class.equals(type) || CharSequence.class.isAssignableFrom(type)) {
			return "VARCHAR";
		}
		if (Integer.class.equals(type) || int.class.equals(type)) {
			return "INTEGER";
		}
		if (Long.class.equals(type) || long.class.equals(type)) {
			return "BIGINT";
		}
		if (Short.class.equals(type) || short.class.equals(type)) {
			return "SMALLINT";
		}
		if (Double.class.equals(type) || double.class.equals(type)) {
			return "DOUBLE PRECISION";
		}
		if (Float.class.equals(type) || float.class.equals(type)) {
			return "REAL";
		}
		if (BigDecimal.class.equals(type) || BigInteger.class.equals(type)) {
			return "NUMERIC";
		}
		if (Boolean.class.equals(type) || boolean.class.equals(type)) {
			return "BOOLEAN";
		}
		if (LocalDate.class.equals(type)) {
			return "DATE";
		}
		if (LocalDateTime.class.equals(type) || Instant.class.equals(type)) {
			return "TIMESTAMP";
		}
		if (java.util.Date.class.equals(type)) {
			return "TIMESTAMP";
		}
		if (UUID.class.equals(type)) {
			return "UUID";
		}
		return null;
	}
}
