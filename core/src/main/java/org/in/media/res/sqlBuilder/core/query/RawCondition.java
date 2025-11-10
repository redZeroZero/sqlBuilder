package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.ConditionValue;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.Operator;

final class RawCondition implements Condition {

	private final Operator startOperator;
	private final RawSqlFragment fragment;

	RawCondition(Operator startOperator, RawSqlFragment fragment) {
		this.startOperator = startOperator;
		this.fragment = Objects.requireNonNull(fragment, "fragment");
	}

	RawCondition withStartOperator(Operator operator) {
		if (Objects.equals(this.startOperator, operator)) {
			return this;
		}
		return new RawCondition(operator, fragment);
	}

	@Override
	public List<ConditionValue> values() {
		if (fragment.parameters().isEmpty()) {
			return List.of();
		}
		List<ConditionValue> values = new ArrayList<>(fragment.parameters().size());
		for (SqlParameter<?> parameter : fragment.parameters()) {
			values.add(ConditionValue.of(parameter));
		}
		return values;
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
		return null;
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
		if (startOperator == null) {
			return fragment.sql();
		}
		String sql = fragment.sql();
		if (sql.isBlank()) {
			return startOperator.value();
		}
		if (Character.isWhitespace(sql.charAt(0))) {
			return startOperator.value() + sql;
		}
		return startOperator.value() + ' ' + sql;
	}
}
