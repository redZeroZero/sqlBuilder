package org.in.media.res.sqlBuilder.api.query.spi;

import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;

/**
 * Clause capturing column assignments for an {@code UPDATE} statement.
 */
public interface SetClause extends Clause, Transpilable {

	SetClause set(Column column, Object value);

	SetClause set(Column column, SqlParameter<?> parameter);

	default SetClause set(TableDescriptor<?> descriptor, Object value) {
		return set(descriptor.column(), value);
	}

	default SetClause set(TableDescriptor<?> descriptor, SqlParameter<?> parameter) {
		return set(descriptor.column(), parameter);
	}

	SetClause setNull(Column column);

	default SetClause setNull(TableDescriptor<?> descriptor) {
		return setNull(descriptor.column());
	}

	default SetClause setRaw(String sql) {
		return setRaw(RawSql.of(sql));
	}

	default SetClause setRaw(String sql, SqlParameter<?>... params) {
		return setRaw(RawSql.of(sql, params));
	}

	SetClause setRaw(RawSqlFragment fragment);

	List<Assignment> assignments();

	/**
	 * Lightweight representation of a {@code SET} entry.
	 */
	final class Assignment {
		private final Column column;
		private final AssignmentValue value;
		private final RawSqlFragment rawFragment;

		private Assignment(Column column, AssignmentValue value, RawSqlFragment rawFragment) {
			this.column = column;
			this.value = value;
			this.rawFragment = rawFragment;
		}

		public static Assignment columnValue(Column column, AssignmentValue value) {
			Objects.requireNonNull(column, "column");
			Objects.requireNonNull(value, "value");
			return new Assignment(column, value, null);
		}

		public static Assignment raw(RawSqlFragment fragment) {
			Objects.requireNonNull(fragment, "fragment");
			return new Assignment(null, null, fragment);
		}

		public Column column() {
			return column;
		}

		public AssignmentValue value() {
			return value;
		}

		public RawSqlFragment rawFragment() {
			return rawFragment;
		}

		public boolean isRaw() {
			return rawFragment != null;
		}
	}

	final class AssignmentValue {
		private final SqlParameter<?> parameter;
		private final Object literal;

		private AssignmentValue(SqlParameter<?> parameter, Object literal) {
			this.parameter = parameter;
			this.literal = literal;
		}

		public static AssignmentValue parameter(SqlParameter<?> parameter) {
			return new AssignmentValue(Objects.requireNonNull(parameter, "parameter"), null);
		}

		public static AssignmentValue literal(Object literal) {
			return new AssignmentValue(null, literal);
		}

		public boolean isParameter() {
			return parameter != null;
		}

		public SqlParameter<?> parameter() {
			return parameter;
		}

		public Object literal() {
			return literal;
		}
	}
}
