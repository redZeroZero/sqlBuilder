package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.Test;

class OptionalConditionsTest {

	private static final TestTable EMPLOYEE = new TestTable("Employee", "ID", "NAME", "SALARY");

	@Test
	void optionalEqualsProducesGuardedPredicate() {
		SqlParameter<String> nameParam = SqlParameters.param("name");
		Query query = SqlQuery.newQuery()
				.select(EMPLOYEE.column("NAME"))
				.from(EMPLOYEE)
				.whereOptionalEquals(EMPLOYEE.column("NAME"), nameParam)
				.asQuery();

		CompiledQuery compiled = query.compile();
		assertEquals(
				"SELECT \"Employee\".\"NAME\" FROM \"Employee\" WHERE (? IS NULL OR \"Employee\".\"NAME\" = ?)",
				compiled.sql());

		Map<String, Object> disabledParams = new HashMap<>();
		disabledParams.put("name", null);
		SqlAndParams disabled = compiled.bind(disabledParams);
		assertEquals(java.util.Arrays.asList(null, null), disabled.params());
	}

	@Test
	void optionalLikeSupportsParameters() {
		SqlParameter<String> pattern = SqlParameters.param("pattern");
		CompiledQuery compiled = SqlQuery.newQuery()
				.select(EMPLOYEE.column("NAME"))
				.from(EMPLOYEE)
				.whereOptionalLike(EMPLOYEE.column("NAME"), pattern)
				.asQuery()
				.compile();

		assertEquals(
				"SELECT \"Employee\".\"NAME\" FROM \"Employee\" WHERE (? IS NULL OR \"Employee\".\"NAME\" LIKE ? ESCAPE '\\')",
				compiled.sql());

		SqlAndParams enabled = compiled.bind(Map.of("pattern", "%ALICE%"));
		assertEquals(List.of("%ALICE%", "%ALICE%"), enabled.params());
	}

	@Test
	void optionalGreaterOrEqualOrdersParametersCorrectly() {
		SqlParameter<Integer> minSalary = SqlParameters.param("minSalary");
		CompiledQuery compiled = SqlQuery.newQuery()
				.select(EMPLOYEE.column("SALARY"))
				.from(EMPLOYEE)
				.whereOptionalGreaterOrEqual(EMPLOYEE.column("SALARY"), minSalary)
				.asQuery()
				.compile();

		assertEquals(
				"SELECT \"Employee\".\"SALARY\" FROM \"Employee\" WHERE (? IS NULL OR \"Employee\".\"SALARY\" >= ?)",
				compiled.sql());

		SqlAndParams enabled = compiled.bind(Map.of("minSalary", 50000));
		assertEquals(List.of(50000, 50000), enabled.params());
	}

	private static final class TestTable implements Table {
		private final String name;
		private final Map<String, Column> columns = new LinkedHashMap<>();

		private TestTable(String name, String... columnNames) {
			this.name = name;
			for (String columnName : columnNames) {
				Column column = org.in.media.res.sqlBuilder.core.model.ColumnImpl.builder()
						.name(columnName)
						.table(this)
						.build();
				columns.put(columnName, column);
			}
		}

		Column column(String columnName) {
			Column column = columns.get(columnName);
			if (column == null) {
				throw new IllegalArgumentException("Unknown column " + columnName);
			}
			return column;
		}

		@Override
		public String getName() {
			return name;
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
			return columns.values().toArray(new Column[0]);
		}

		@Override
		public Column get(String columnName) {
			return columns.get(columnName);
		}

		@Override
		public Column get(TableDescriptor<?> descriptor) {
			if (descriptor == null) {
				return null;
			}
			return columns.get(descriptor.value());
		}

		@Override
		public void includeSchema(String schema) {
			// not required for tests
		}

		@Override
		public boolean hasTableName() {
			return true;
		}

		@Override
		public String tableName() {
			return name;
		}
	}
}
