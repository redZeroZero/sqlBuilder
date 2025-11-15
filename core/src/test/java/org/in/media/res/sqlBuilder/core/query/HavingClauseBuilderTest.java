package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.QueryHavingBuilder;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class HavingClauseBuilderTest {

	@Test
	void fluentHavingBuilderSupportsAggregatesAndSubqueries() {
		Query subquery = SqlQuery.newQuery().asQuery();
		subquery.select(TestSchema.EMP_SALARY.column())
				.from(TestSchema.EMPLOYEES)
				.groupBy(TestSchema.EMP_DEPT_ID.column())
				.having(TestSchema.EMP_SALARY.column()).avg(TestSchema.EMP_SALARY.column()).supOrEqTo(1_000.0);

		SqlParameter<Double> lowerBound = SqlParameters.param("lowerBound", Double.class);
		SqlParameter<Double> upperBound = SqlParameters.param("upperBound", Double.class);

		Query query = SqlQuery.newQuery().asQuery();
		QueryHavingBuilder havingBuilder = query.select(TestSchema.EMP_DEPT_ID.column())
				.from(TestSchema.EMPLOYEES)
				.groupBy(TestSchema.EMP_DEPT_ID.column())
				.having(TestSchema.EMP_SALARY.column());

		query = havingBuilder.min(TestSchema.EMP_SALARY.column()).supOrEqTo(500.0);
		query.having(TestSchema.EMP_SALARY.column()).max(TestSchema.EMP_SALARY.column()).infOrEqTo(10_000.0);
		query.having(TestSchema.EMP_SALARY.column()).avg(TestSchema.EMP_SALARY.column()).between(lowerBound, upperBound);
		query.having(TestSchema.EMP_SALARY.column()).sum(TestSchema.EMP_SALARY.column()).in(1_000.0, 2_000.0);
		String sql = query.transpile();
		assertThat(sql).contains("HAVING MIN(\"E\".\"EMP_SALARY\") >= ?");
		assertThat(sql).contains("MAX(\"E\".\"EMP_SALARY\") <= ?");
		assertThat(sql).contains("AVG(\"E\".\"EMP_SALARY\") BETWEEN ?");
		assertThat(sql).contains("SUM(\"E\".\"EMP_SALARY\")");

		CompiledQuery compiled = query.compile();
		List<String> parameterNames = compiled.placeholders().stream()
				.filter(ph -> ph.parameter() != null)
				.map(ph -> ph.parameter().name())
				.toList();

		assertThat(parameterNames).contains(
				lowerBound.name(),
				upperBound.name());
	}
}
