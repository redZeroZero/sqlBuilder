package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.spi.SetClause;

final class SetClauseImpl implements SetClause {

	private final List<Assignment> assignments = new ArrayList<>();

	@Override
	public SetClause set(Column column, Object value) {
		if (value == null) {
			return setNull(column);
		}
		assignments.add(Assignment.columnValue(requireColumn(column), AssignmentValue.literal(value)));
		return this;
	}

	@Override
	public SetClause set(Column column, SqlParameter<?> parameter) {
		assignments.add(Assignment.columnValue(requireColumn(column), AssignmentValue.parameter(parameter)));
		return this;
	}

	@Override
	public SetClause setNull(Column column) {
		assignments.add(Assignment.columnValue(requireColumn(column), AssignmentValue.literal(null)));
		return this;
	}

	@Override
	public SetClause setRaw(RawSqlFragment fragment) {
		assignments.add(Assignment.raw(fragment));
		return this;
	}

	@Override
	public List<Assignment> assignments() {
		return Collections.unmodifiableList(assignments);
	}

	@Override
	public String transpile() {
		if (assignments.isEmpty()) {
			throw new IllegalStateException("SET clause requires at least one assignment");
		}
		StringBuilder builder = new StringBuilder(" SET ");
		for (int i = 0; i < assignments.size(); i++) {
			Assignment assignment = assignments.get(i);
			if (assignment.isRaw()) {
				builder.append(assignment.rawFragment().sql());
			} else {
				builder.append(quoteColumnName(assignment.column())).append(valueExpression(assignment.value()));
			}
			if (i < assignments.size() - 1) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	private String quoteColumnName(Column column) {
		return org.in.media.res.sqlBuilder.core.query.dialect.DialectContext.current().quoteIdent(column.getName());
	}

	private String valueExpression(AssignmentValue value) {
		if (!value.isParameter() && value.literal() == null) {
			return " = NULL";
		}
		return " = ?";
	}

	private Column requireColumn(Column column) {
		if (column == null) {
			throw new IllegalArgumentException("column must not be null");
		}
		return column;
	}
}
