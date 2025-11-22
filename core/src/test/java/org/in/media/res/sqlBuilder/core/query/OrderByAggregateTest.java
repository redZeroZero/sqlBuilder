package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class OrderByAggregateTest {

	@Test
	void orderByAggregateRendersFunction() {
		SqlAndParams sap = SqlQuery.query()
				.select(AggregateOperator.SUM, TestSchema.EMP_SALARY.column())
				.from(TestSchema.EMPLOYEES)
				.groupBy(TestSchema.EMP_DEPT_ID)
				.orderByAggregate(AggregateOperator.SUM, TestSchema.EMP_SALARY.column(), SortDirection.DESC)
				.render();

		assertThat(sap.sql()).contains("ORDER BY SUM(\"E\".\"EMP_SALARY\") DESC");
	}

	@Test
	void orderByAliasUsesQuotedIdentifier() {
		SqlAndParams sap = SqlQuery.query()
				.select(TestSchema.EMP_NAME)
				.from(TestSchema.EMPLOYEES)
				.orderByAlias("EMP_NAME", SortDirection.ASC)
				.render();

		assertThat(sap.sql()).contains("ORDER BY \"EMP_NAME\" ASC");
	}

	@Test
	void orderByFirstAggregatePicksFirstAggregate() {
		SqlAndParams sap = SqlQuery.query()
				.select(AggregateOperator.SUM, TestSchema.EMP_SALARY.column())
				.select(AggregateOperator.COUNT, TestSchema.EMP_ID.column())
				.from(TestSchema.EMPLOYEES)
				.groupBy(TestSchema.EMP_DEPT_ID)
				.orderByFirstAggregate(SortDirection.DESC)
				.render();

		assertThat(sap.sql()).contains("ORDER BY SUM(\"E\".\"EMP_SALARY\") DESC");
	}
}
