package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.Test;

class RawFragmentFlowTest {

	private final Table employee = Tables.builder("Employee", "E")
			.column("ID")
			.column("DEPT_ID")
			.column("SALARY")
			.build();

	@Test
	void rawFragmentsPreserveParameterOrderingAcrossClauses() {
		SqlParameter<Integer> minSalary = SqlParameters.param("minSalary", Integer.class);
		SqlParameter<Integer> maxSalary = SqlParameters.param("maxSalary", Integer.class);

		Query query = (Query) SqlQuery.newQuery();
		query.selectRaw("\"E\".\"DEPT_ID\", COUNT(*) AS total");
		query.from(employee);
		query.whereRaw("\"E\".\"SALARY\" >= ?", minSalary);
		query.andRaw("\"E\".\"SALARY\" <= ?", maxSalary);
		query.groupBy(employee.get("DEPT_ID"));
		query.groupByRaw("ROLLUP(\"E\".\"DEPT_ID\")");
		query.orderBy(employee.get("DEPT_ID"));
		query.orderByRaw("\"E\".\"DEPT_ID\" ASC");

		CompiledQuery compiled = query.compile();

		assertThat(compiled.placeholders())
				.extracting(ph -> ph.parameter().name())
				.containsExactly("minSalary", "maxSalary");

		SqlAndParams bound = compiled.bind(80_000, 150_000);
		assertThat(bound.params()).containsExactly(80_000, 150_000);
		assertThat(bound.sql()).contains("GROUP BY \"E\".\"DEPT_ID\", ROLLUP(\"E\".\"DEPT_ID\")");
		assertThat(bound.sql()).contains("ORDER BY \"E\".\"DEPT_ID\"");
	}
}
