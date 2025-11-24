package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.CteRef;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.WithBuilder;
import org.in.media.res.sqlBuilder.api.query.WithChain;
import org.in.media.res.sqlBuilder.api.query.SetOperator;
import org.in.media.res.sqlBuilder.core.model.ColumnImpl;
import org.junit.jupiter.api.Test;

class WithQueryTest {

	private static final TestTable EMPLOYEE = new TestTable("Employee", "ID", "SALARY");

	@Test
	void chainedWithBuilderRegistersCtesInOrder() {
		Query first = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.from(EMPLOYEE)
				.where(EMPLOYEE.column("SALARY"))
				.eq(1000)
				.asQuery();

		Query second = SqlQuery.newQuery()
				.select(EMPLOYEE.column("SALARY"))
				.from(EMPLOYEE)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		WithBuilder.CteStep firstStep = with.with("first_cte", first, "ID");
		CteRef firstRef = firstStep.ref();
		CteRef secondRef = firstStep.and().with("second_cte", second, "SALARY").ref();

		Query main = with.main(SqlQuery.newQuery()
				.select(firstRef.column("ID"))
				.select(secondRef.column("SALARY"))
				.from(firstRef)
				.join(secondRef).on(firstRef.column("ID"), secondRef.column("SALARY"))
				.where(secondRef.column("SALARY")).supOrEqTo(5000)
				.asQuery());

		SqlAndParams rendered = main.render();

		assertTrue(rendered.sql().indexOf("\"first_cte\"") < rendered.sql().indexOf("\"second_cte\""));
		assertEquals(List.of(1000, 5000), rendered.params());
	}

	@Test
	void stagedWithBuilderRegistersCteViaAs() {
		Query first = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.from(EMPLOYEE)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		WithBuilder.CteStep step = with.with("staged_cte").as(first, "EMP_ID");
		CteRef ref = step.ref();

		Query main = step.and().main(SqlQuery.newQuery()
				.select(ref.column("EMP_ID"))
				.from(ref)
				.asQuery());

		SqlAndParams rendered = main.render();

		assertTrue(rendered.sql().startsWith("WITH \"staged_cte\""));
		assertEquals(List.of(), rendered.params());
	}

	@Test
	void withChainBuildsMainQueryInOneFlow() {
		Query cteQuery = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.from(EMPLOYEE)
				.where(EMPLOYEE.column("SALARY"))
				.eq(700)
				.asQuery();

		WithChain chain = SqlQuery.withChain()
				.cte("thresholds", cteQuery, "EMP_ID");

		Query main = SqlQuery.newQuery()
				.select(chain.ref("thresholds").column("EMP_ID"))
				.from(chain.ref("thresholds"))
				.asQuery();

		SqlAndParams rendered = chain.attach(main).render();

		assertTrue(rendered.sql().startsWith("WITH \"thresholds\""));
		assertEquals(List.of(700), rendered.params());
	}

	@Test
	void withChainAttachLambdaSupportsMultipleCtes() {
		Query first = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.from(EMPLOYEE)
				.asQuery();

		Query second = SqlQuery.newQuery()
				.select(EMPLOYEE.column("SALARY"))
				.from(EMPLOYEE)
				.asQuery();

		SqlAndParams rendered = SqlQuery.withChain()
				.cte("first", first, "EMP_ID")
				.cte("second", second, "SALARY")
				.attach(chain -> SqlQuery.newQuery()
						.select(chain.ref("first").column("EMP_ID"))
						.select(chain.ref("second").column("SALARY"))
						.from(chain.ref("first"))
						.join(chain.ref("second")).on(chain.ref("first").column("EMP_ID"),
								chain.ref("second").column("SALARY"))
						.asQuery())
				.render();

		assertTrue(rendered.sql().startsWith("WITH \"first\""));
		assertTrue(rendered.sql().contains("\"second\""));
		assertEquals(List.of(), rendered.params());
	}

	@Test
	void withChainRejectsUnknownRef() {
		WithChain chain = SqlQuery.withChain();
		assertThrows(IllegalArgumentException.class, () -> chain.ref("missing"));
	}

	@Test
	void chainedWithBuilderRejectsDuplicateName() {
		Query simple = SqlQuery.newQuery().select(EMPLOYEE.column("ID")).from(EMPLOYEE).asQuery();

		WithBuilder with = SqlQuery.with();
		WithBuilder.CteStep firstStep = with.with("dup_chain", simple, "ID");
		firstStep.ref();

		assertThrows(IllegalArgumentException.class, () -> firstStep.and().with("dup_chain", simple, "ID"));
	}

	@Test
	void chainedWithBuilderRejectsDuplicateAliases() {
		Query twoColumns = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.select(EMPLOYEE.column("SALARY"))
				.from(EMPLOYEE)
				.asQuery();

		WithBuilder with = SqlQuery.with();

		assertThrows(IllegalArgumentException.class, () -> with.with("alias_dup", twoColumns, "COL", "COL"));
	}

	@Test
	void renderIncludesWithClauseAndParams() {
		Query salaryFilter = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.select(EMPLOYEE.column("SALARY"))
				.from(EMPLOYEE)
				.where(EMPLOYEE.column("SALARY"))
				.supOrEqTo(1000)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		CteRef salaryCte = with.cte("salary_cte", salaryFilter, "employee_id", "total_salary");

		Query main = with.main(SqlQuery.newQuery()
				.select(salaryCte.column("employee_id"))
				.from(salaryCte)
				.asQuery());

		SqlAndParams result = main.render();

		assertEquals(
				"WITH \"salary_cte\"(\"employee_id\", \"total_salary\") AS (SELECT \"Employee\".\"ID\", \"Employee\".\"SALARY\" FROM \"Employee\" WHERE \"Employee\".\"SALARY\" >= ?) SELECT \"salary_cte\".\"employee_id\" FROM \"salary_cte\"",
				result.sql());
		assertEquals(List.of(1000), result.params());
	}

	@Test
	void compileBindsParametersAcrossCtesAndMainQuery() {
		SqlParameter<Integer> minSalary = SqlParameters.param("minSalary");
		SqlParameter<Integer> targetId = SqlParameters.param("targetId");

		Query cteQuery = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.from(EMPLOYEE)
				.where(EMPLOYEE.column("SALARY"))
				.supOrEqTo(minSalary)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		CteRef filtered = with.cte("filtered_emp", cteQuery, "EMP_ID");

		Query main = with.main(SqlQuery.newQuery()
				.select(filtered.column("EMP_ID"))
				.from(filtered)
				.where(filtered.column("EMP_ID"))
				.eq(targetId)
				.asQuery());

		CompiledQuery compiled = main.compile();
		assertTrue(compiled.sql().startsWith("WITH \"filtered_emp\""));

		SqlAndParams bound = compiled.bind(Map.of("minSalary", 90000, "targetId", 7));
		assertEquals(List.of(90000, 7), bound.params());
	}

	@Test
	void paramsFromCtePrecedeMainQueryParams() {
		Query salaryFilter = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.from(EMPLOYEE)
				.where(EMPLOYEE.column("SALARY"))
				.eq(2000)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		CteRef cte = with.cte("thresholds", salaryFilter, "employee_id");

		Query main = with.main(SqlQuery.newQuery()
				.select(cte.column("employee_id"))
				.from(cte)
				.where(cte.column("employee_id"))
				.eq(99)
				.asQuery());

		SqlAndParams rendered = main.render();
		assertEquals(List.of(2000, 99), rendered.params());
	}

	@Test
	void duplicateCteNamesAreRejected() {
		WithBuilder with = SqlQuery.with();
		Query simple = SqlQuery.newQuery().select(EMPLOYEE.column("ID")).from(EMPLOYEE).asQuery();
		with.cte("dup", simple);
		assertThrows(IllegalArgumentException.class, () -> with.cte("dup", simple));
	}

	@Test
	void aliasCountMustMatchProjection() {
		WithBuilder with = SqlQuery.with();
		Query simple = SqlQuery.newQuery()
				.select(EMPLOYEE.column("ID"))
				.select(EMPLOYEE.column("SALARY"))
				.from(EMPLOYEE)
				.asQuery();
		assertThrows(IllegalArgumentException.class, () -> with.cte("mismatch", simple, "only_one"));
	}

	@Test
	void unsupportedDialectsFailFast() {
		Dialect noCteDialect = new Dialect() {
			@Override
			public String id() {
				return "noc";
			}

			@Override
			public String quoteIdent(String raw) {
				return raw;
			}

			@Override
			public char likeEscapeChar() {
				return '\\';
			}

			@Override
			public String setOperator(SetOperator operator) {
				return operator.sql();
			}

			@Override
			public String renderFunction(String logicalName, List<String> argsSql) {
				return logicalName + '(' + String.join(", ", argsSql) + ')';
			}

			@Override
			public PaginationClause renderLimitOffset(Long limit, Long offset) {
				return PaginationClause.empty();
			}

			@Override
			public boolean supportsCte() {
				return false;
			}
		};

		Query cteQuery = SqlQuery.newQuery(noCteDialect)
				.select(EMPLOYEE.column("ID"))
				.from(EMPLOYEE)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		CteRef cte = with.cte("disallowed", cteQuery, "employee_id");

		Query main = with.main(SqlQuery.newQuery(noCteDialect)
				.select(cte.column("employee_id"))
				.from(cte)
				.asQuery());

		assertThrows(UnsupportedOperationException.class, main::render);
	}

	private static final class TestTable implements Table {
		private final String name;
		private final Map<String, Column> columns = new LinkedHashMap<>();

		private TestTable(String name, String... columnNames) {
			this.name = name;
			for (String columnName : columnNames) {
				Column column = ColumnImpl.builder().name(columnName).table(this).build();
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
