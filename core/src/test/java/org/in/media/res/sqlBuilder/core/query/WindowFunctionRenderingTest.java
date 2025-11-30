package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.window.WindowExpression;
import org.in.media.res.sqlBuilder.api.query.window.WindowFunction;
import org.in.media.res.sqlBuilder.api.query.window.WindowFunctions;
import org.in.media.res.sqlBuilder.api.query.window.WindowFrame;
import org.in.media.res.sqlBuilder.api.query.window.WindowOrdering;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class WindowFunctionRenderingTest {

	@Test
	void rendersWindowFunctionWithPartitionOrderAndFrame() {
		WindowFunction runningTotal = WindowFunctions.sum(TestSchema.EMP_SALARY.column())
				.partitionBy(TestSchema.EMP_DEPT_ID.column())
				.orderBy(TestSchema.EMP_HIRED_AT.column(), SortDirection.DESC)
				.rowsBetween(WindowFrame.Bound.unboundedPreceding(), WindowFrame.Bound.currentRow())
				.as("RUNNING_TOTAL");

		Query query = SqlQuery.query();
		query.select(TestSchema.EMP_NAME.column());
		query.select(runningTotal);
		query.from(TestSchema.EMPLOYEES);

		String sql = query.transpile();

		assertThat(sql)
				.contains("SUM(\"E\".\"EMP_SALARY\") OVER(PARTITION BY \"E\".\"EMP_DEPT_ID\" ORDER BY \"E\".\"EMP_HIRED_AT\" DESC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)")
				.contains("AS \"RUNNING_TOTAL\"");
	}

	@Test
	void collectsPlaceholdersFromRawWindowExpressions() {
		Table t = Tables.builder("sample", "s")
				.column("id")
				.column("value")
				.build();

		SqlParameter<Integer> partitionParam = SqlParameters.param("p", Integer.class);
		SqlParameter<Integer> orderParam = SqlParameters.param("o", Integer.class);

		WindowFunction window = WindowFunctions.rowNumber()
				.partitionBy(WindowExpression.raw("\"s\".\"value\" + ?", partitionParam))
				.orderBy(WindowOrdering.desc(WindowExpression.raw("\"s\".\"id\" + ?", orderParam)));

		Query query = SqlQuery.query();
		query.select(window);
		query.from(t);

		CompiledQuery compiled = query.compile();

		assertThat(compiled.placeholders())
				.extracting(CompiledQuery.Placeholder::parameter)
				.containsExactlyElementsOf(java.util.List.of(partitionParam, orderParam));
	}

	@Test
	void rendersCountStarWindow() {
		Query query = SqlQuery.query();
		query.select(WindowFunctions.countStar().as("cnt"));
		query.from(TestSchema.EMPLOYEES);

		assertThat(query.transpile()).contains("COUNT(*) OVER() AS \"cnt\"");
	}

	@Test
	void rendersNtileWithColumnRefs() {
		var ntile = WindowFunctions.ntile(4)
				.partitionBy(TestSchema.EMP_DEPT_ID)
				.orderBy(TestSchema.EMP_SALARY);

		Query query = SqlQuery.query();
		query.select(ntile);
		query.from(TestSchema.EMPLOYEES);

		assertThat(query.transpile()).contains("NTILE(4) OVER(PARTITION BY \"E\".\"EMP_DEPT_ID\" ORDER BY \"E\".\"EMP_SALARY\" ASC)");
	}

	@Test
	void rendersLagWithOrderingAndAlias() {
		var lag = WindowFunctions.lag(TestSchema.EMP_SALARY.column(), 2)
				.orderBy(TestSchema.EMP_HIRED_AT.column(), SortDirection.ASC)
				.as("prev_sal");

		Query query = SqlQuery.query();
		query.select(lag);
		query.from(TestSchema.EMPLOYEES);

		assertThat(query.transpile())
				.contains("LAG(\"E\".\"EMP_SALARY\", 2) OVER(ORDER BY \"E\".\"EMP_HIRED_AT\" ASC)")
				.contains("AS \"prev_sal\"");
	}
}
