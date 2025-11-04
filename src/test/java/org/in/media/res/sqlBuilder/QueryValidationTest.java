package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.HavingBuilder;
import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.core.query.ConditionGroupBuilder;
import org.in.media.res.sqlBuilder.core.query.HavingImpl;
import org.in.media.res.sqlBuilder.core.query.WhereImpl;
import org.junit.jupiter.api.Test;

class QueryValidationTest {

	private static final Table TABLE = new Table() {
		@Override
		public String getName() {
			return "FAKE";
		}

		@Override
		public String getAlias() {
			return null;
		}

		@Override
		public boolean hasAlias() {
			return false;
		}

		@Override
		public Column[] getColumns() {
			return new Column[0];
		}

		@Override
		public Column get(String columnName) {
			return null;
		}

		@Override
		public Column get(TableDescriptor<?> descriptor) {
			return null;
		}

		@Override
		public void includeSchema(String schema) {
			// no-op
		}

		@Override
		public boolean hasTableName() {
			return true;
		}

		@Override
		public String tableName() {
			return getName();
		}
	};

	private static final class ColumnStub implements Column {
		private final Table table;

		private ColumnStub(Table table) {
			this.table = table;
		}

		@Override
		public String transpile(boolean useAlias) {
			return "STUB";
		}

		@Override
		public String transpile() {
			return transpile(true);
		}

	@Override
	public Table table() {
		return table;
	}

	@Override
	public String getName() {
		return "COLUMN";
	}

	@Override
	public String getAlias() {
		return null;
	}

	@Override
	public boolean hasColumnAlias() {
		return false;
	}
}

	@Test
	void whereRejectsColumnsWithoutTable() {
		Where where = new WhereImpl();
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> where.where(new ColumnStub(null)));
		assertTrue(ex.getMessage().contains("Column must belong to a table"));
	}

	@Test
	void havingRejectsColumnsWithoutTable() {
		Having having = new HavingImpl();
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> having.having(new ColumnStub(null)));
		assertTrue(ex.getMessage().contains("Column must belong to a table"));
	}

	@Test
	void validationAcceptsColumnsWithTable() {
		Where where = new WhereImpl();
		where.where(new ColumnStub(TABLE));
	}

	@Test
	void whereInRejectsEmptyValueLists() {
		Where where = new WhereImpl();
		where.where(new ColumnStub(TABLE));
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> where.in(new String[0]));
		assertTrue(ex.getMessage().contains("at least one value"));
	}

	@Test
	void havingInRejectsEmptyValueLists() {
		Having having = new HavingImpl();
		HavingBuilder builder = having.having(new ColumnStub(TABLE));
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> builder.in(new Number[0]));
		assertTrue(ex.getMessage().contains("at least one value"));
	}

	@Test
	void conditionGroupInRejectsEmptyValueLists() {
		ConditionGroupBuilder builder = new ConditionGroupBuilder();
		builder.where(new ColumnStub(TABLE));
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> builder.in(new String[0]));
		assertTrue(ex.getMessage().contains("at least one value"));
	}
}
