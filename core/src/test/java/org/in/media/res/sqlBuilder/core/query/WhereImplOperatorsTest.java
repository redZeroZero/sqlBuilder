package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class WhereImplOperatorsTest {

	@Test
	void literalOverloadsProduceExpectedSqlAndParameters() {
		Date hiredFrom = new Date(0);
		Date hiredTo = new Date(1_000_000);
		Date hiredOn = new Date(2_000_000);

		Query query = SqlQuery.query();
		query.select(TestSchema.EMP_ID.column()).from(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_NAME.column()).eq("Alice")
				.and(TestSchema.EMP_NAME.column()).notEq("Bob")
				.and(TestSchema.EMP_NAME.column()).like("A%")
				.and(TestSchema.EMP_NAME.column()).notLike("B%")
				.and(TestSchema.EMP_ID.column()).between(10, 99)
				.and(TestSchema.EMP_ID.column()).in(1, 2, 3)
				.and(TestSchema.EMP_ID.column()).notIn(7, 8)
				.and(TestSchema.EMP_SALARY.column()).between(100.0, 999.0)
				.and(TestSchema.EMP_SALARY.column()).notIn(200.0)
				.and(TestSchema.EMP_HIRED_AT.column()).between(hiredFrom, hiredTo)
				.and(TestSchema.EMP_HIRED_AT.column()).in(hiredOn);

		String sql = query.transpile();
		assertThat(sql).contains("WHERE \"E\".\"EMP_NAME\" = ?");
		assertThat(sql).contains("AND \"E\".\"EMP_NAME\" NOT LIKE ?");
		assertThat(sql).contains("AND \"E\".\"EMP_ID\" BETWEEN ? AND ?");
		assertThat(sql).contains("AND \"E\".\"EMP_ID\" IN (?, ?, ?)");
		assertThat(sql).contains("AND \"E\".\"EMP_SALARY\" BETWEEN ? AND ?");

		SqlAndParams rendered = query.render();
		List<Object> params = rendered.params();
		assertThat(params).containsExactly(
				"Alice", "Bob", "A\\%", "B\\%",
				10, 99,
				1, 2, 3,
				7, 8,
				100.0, 999.0,
				200.0,
				hiredFrom, hiredTo,
				hiredOn);
	}

	@Test
	void subqueriesAndConnectorsAreSupported() {
		Query salarySubquery = SqlQuery.query();
		salarySubquery.select(TestSchema.EMP_ID.column())
				.from(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_SALARY.column()).supTo(500.0);

		Query query = SqlQuery.query();
		query.select(TestSchema.EMP_ID.column()).from(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_ID.column()).col(TestSchema.EMP_DEPT_ID.column()).eq(99)
				.and(TestSchema.EMP_ID.column()).supTo(TestSchema.EMP_DEPT_ID.column())
				.and(TestSchema.EMP_ID.column()).supOrEqTo(TestSchema.EMP_DEPT_ID.column())
				.and(TestSchema.EMP_ID.column()).infTo(TestSchema.EMP_DEPT_ID.column())
				.and(TestSchema.EMP_ID.column()).infOrEqTo(TestSchema.EMP_DEPT_ID.column())
				.and(TestSchema.EMP_ID.column()).eq(salarySubquery)
				.and(TestSchema.EMP_ID.column()).notEq(salarySubquery)
				.and(TestSchema.EMP_ID.column()).in(salarySubquery)
				.and(TestSchema.EMP_ID.column()).notIn(salarySubquery)
				.and(TestSchema.EMP_ID.column()).supTo(salarySubquery)
				.and(TestSchema.EMP_ID.column()).supOrEqTo(salarySubquery)
				.and(TestSchema.EMP_ID.column()).infTo(salarySubquery)
				.and(TestSchema.EMP_ID.column()).infOrEqTo(salarySubquery)
				.and().exists(salarySubquery)
				.or().notExists(salarySubquery)
				.and(TestSchema.EMP_NAME.column()).isNull()
				.or(TestSchema.EMP_NAME.column()).isNotNull();

		String sql = query.transpile();
		assertThat(sql).contains("\"E\".\"EMP_ID\" > \"E\".\"EMP_DEPT_ID\"");
		assertThat(sql).contains("EXISTS (SELECT");
		assertThat(sql).contains("NOT EXISTS (SELECT");
		assertThat(sql).contains("IS NULL");
		assertThat(sql).contains("IS NOT NULL");

		CompiledQuery compiled = query.compile();
		assertThat(compiled.placeholders())
				.hasSizeGreaterThanOrEqualTo(1);
	}
}
