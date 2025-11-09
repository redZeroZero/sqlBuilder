package org.in.media.res.sqlBuilder.core.query;

import java.util.Objects;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.core.query.predicate.ParameterCondition;

/**
 * Factory helpers for optional predicate groups of the form
 * {@code (param IS NULL OR column <op> param)}.
 */
public final class OptionalConditions {

	private OptionalConditions() {
	}

	public static Condition optionalEquals(Column column, SqlParameter<?> parameter) {
		return buildGroup(parameter, group -> group.or(column).eq(parameter));
	}

	public static Condition optionalLike(Column column, SqlParameter<?> parameter) {
		return buildGroup(parameter, group -> group.or(column).like(parameter));
	}

	public static Condition optionalGreaterOrEq(Column column, SqlParameter<?> parameter) {
		return buildGroup(parameter, group -> group.or(column).supOrEqTo(parameter));
	}

	private static Condition buildGroup(SqlParameter<?> parameter, Consumer<ConditionGroupBuilder> whenPresent) {
		Objects.requireNonNull(parameter, "parameter");
		Objects.requireNonNull(whenPresent, "whenPresent");
		ConditionGroupBuilder builder = new ConditionGroupBuilder();
		builder.where(ParameterCondition.isNull(parameter));
		whenPresent.accept(builder);
		return builder.build();
	}
}
