package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;
import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;
import org.junit.jupiter.api.Test;

class DefaultClauseTranspilerTest {

	private final Table employee = Tables.builder("Employee", "E")
			.column("ID")
			.column("DEPT_ID")
			.column("SALARY")
			.build();

	@Test
	void groupByTranspilerCombinesColumnsAndRawFragments() {
		GroupByImpl groupBy = new GroupByImpl();
		groupBy.groupBy(employee.get("ID"));
		groupBy.groupByRaw("ROLLUP(\"E\".\"DEPT_ID\")");

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThat(groupBy.transpile()).isEqualTo(" GROUP BY \"E\".\"ID\", ROLLUP(\"E\".\"DEPT_ID\")");
		}
	}

	@Test
	void orderByTranspilerRendersDirectionsAndRawFragments() {
		OrderByImpl orderBy = new OrderByImpl();
		orderBy.orderBy(employee.get("DEPT_ID"), SortDirection.DESC);
		orderBy.orderByRaw("total ASC");

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThat(orderBy.transpile()).isEqualTo(" ORDER BY \"E\".\"DEPT_ID\" DESC, total ASC");
		}
	}

	@Test
	void whereTranspilerSkipsEmptyConditions() {
		WhereImpl where = new WhereImpl(Dialects.oracle());
		assertThat(where.transpile()).isEmpty();
	}

	@Test
	void whereTranspilerAppendsRawFragments() {
		WhereImpl where = new WhereImpl(Dialects.oracle());
		where.where(employee.get("ID")).eq(SqlParameters.param("id", Long.class));
		where.andRaw("1=1");

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThat(where.transpile()).isEqualTo(" WHERE \"E\".\"ID\" = ? AND  1=1");
		}
	}

	@Test
	void limitTranspilerHandlesOffsetAndLimit() {
		LimitImpl limit = new LimitImpl();
		assertThat(limit.transpile()).isEmpty();
		limit.limitAndOffset(25, 5);
		assertThat(limit.transpile()).isEmpty();
	}
}
