package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.in.media.res.sqlBuilder.implementation.Having;
import org.in.media.res.sqlBuilder.implementation.Where;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;
import org.junit.jupiter.api.Test;

class QueryValidationTest {

	private static final ITable TABLE = new ITable() {
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
		public IColumn[] getColumns() {
			return new IColumn[0];
		}

		@Override
		public IColumn get(String columnName) {
			return null;
		}

		@Override
		public IColumn get(ITableDescriptor<?> descriptor) {
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

	private static final class ColumnStub implements IColumn {
		private final ITable table;

		private ColumnStub(ITable table) {
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
		public ITable table() {
			return table;
		}
	}

	@Test
	void whereRejectsColumnsWithoutTable() {
		Where where = new Where();
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> where.where(new ColumnStub(null)));
		assertTrue(ex.getMessage().contains("Column must belong to a table"));
	}

	@Test
	void havingRejectsColumnsWithoutTable() {
		Having having = new Having();
		IllegalStateException ex = assertThrows(IllegalStateException.class,
				() -> having.having(new ColumnStub(null)));
		assertTrue(ex.getMessage().contains("Column must belong to a table"));
	}

	@Test
	void validationAcceptsColumnsWithTable() {
		Where where = new Where();
		where.where(new ColumnStub(TABLE));
	}
}
