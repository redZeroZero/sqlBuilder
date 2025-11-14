package org.in.media.res.sqlBuilder.api.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class StageInterfaceDefaultsTest {

	@Test
	void selectAndFromStageDescriptorDefaultsDelegateToColumns() {
		SelectStage selectStage = SqlQuery.newQuery();
		selectStage.select(TestSchema.EMP_ID, TestSchema.EMP_NAME);
		selectStage.select(AggregateOperator.COUNT, TestSchema.EMP_ID);
		FromStage fromStage = selectStage.from(TestSchema.EMPLOYEES);

		Query subquery = SqlQuery.newQuery().asQuery();
		subquery.select(TestSchema.EMP_ID.column()).from(TestSchema.EMPLOYEES);

		fromStage.from(subquery, "derived_from", "EMP_ID");
		fromStage.crossJoin(subquery, "cross_alias", "EMP_ID");
		fromStage.on(TestSchema.EMP_ID, TestSchema.EMP_ID);
		fromStage.on(TestSchema.EMP_ID, TestSchema.EMP_ID.column());
		fromStage.on(TestSchema.EMP_ID.column(), TestSchema.EMP_ID);
		fromStage.on(TestSchema.EMP_ID.column(), TestSchema.EMP_ID.column());

		Query query = fromStage.asQuery();
		assertThat(query.transpile()).contains("\"derived_from\"");
		assertThat(query.transpile()).contains("\"cross_alias\"");
	}

	@Test
	void predicateAndQueryColumnRefWrappersApplyConsistentPredicates() {
		Query query = SqlQuery.newQuery().asQuery();
		query.select(TestSchema.EMP_ID.column()).from(TestSchema.EMPLOYEES);
		query.where(TestSchema.EMP_NAME).eq("Alice");
		query.whereOptionalEquals(TestSchema.EMP_NAME, SqlParameters.param("optName", String.class));
		query.whereOptionalLike(TestSchema.EMP_NAME, SqlParameters.param("optPattern", String.class));
		query.like(TestSchema.EMP_NAME, "A%");
		query.notLike(TestSchema.EMP_NAME, "B%");
		query.eq(TestSchema.EMP_NAME, "Carol");
		query.notEq(TestSchema.EMP_NAME, "Dan");
		query.isNull(TestSchema.EMP_NAME);
		query.isNotNull(TestSchema.EMP_NAME);

		query.eq(TestSchema.EMP_ID, 1);
		query.notEq(TestSchema.EMP_ID, 2);
		query.supTo(TestSchema.EMP_ID, 3);
		query.supOrEqTo(TestSchema.EMP_ID, 4);
		query.infTo(TestSchema.EMP_ID, 5);
		query.infOrEqTo(TestSchema.EMP_ID, 6);
		query.between(TestSchema.EMP_ID, 10, 20);
		query.in(TestSchema.EMP_ID, 7, 8);
		query.notIn(TestSchema.EMP_ID, 9, 11);

		query.eq(TestSchema.EMP_SALARY, 100.0);
		query.notEq(TestSchema.EMP_SALARY, 200.0);
		query.between(TestSchema.EMP_SALARY, 100.0, 200.0);
		query.in(TestSchema.EMP_SALARY, 100.0, 300.0);
		query.notIn(TestSchema.EMP_SALARY, 400.0);

		query.eq(TestSchema.EMP_HIRED_AT, new Date(0));
		query.notEq(TestSchema.EMP_HIRED_AT, new Date(1_000_000));
		query.between(TestSchema.EMP_HIRED_AT, new Date(0), new Date(2_000_000));
		query.in(TestSchema.EMP_HIRED_AT, new Date(3_000_000));
		query.notIn(TestSchema.EMP_HIRED_AT, new Date(4_000_000));

		query.groupBy(TestSchema.EMP_DEPT_ID, TestSchema.EMP_NAME);
		query.orderBy(TestSchema.EMP_NAME);
		query.orderBy(TestSchema.EMP_NAME, SortDirection.DESC);
		query.asc(TestSchema.EMP_NAME);
		query.desc(TestSchema.EMP_NAME);

		String sql = query.transpile();
		assertThat(sql).contains("GROUP BY \"E\".\"EMP_DEPT_ID\", \"E\".\"EMP_NAME\"");
		assertThat(sql).contains("ORDER BY \"E\".\"EMP_NAME\"");
	}
}
