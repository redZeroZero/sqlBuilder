package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class UpdateQueryTest {

	@Test
	void renderIncludesLiteralAssignments() {
		SqlAndParams rendered = SqlQuery.update(TestSchema.EMPLOYEES)
				.set(TestSchema.EMP_NAME.column(), "Alice")
				.setNull(TestSchema.EMP_DEPT_ID.column())
				.where(TestSchema.EMP_ID.column()).eq(42)
				.render();

		assertThat(rendered.sql())
				.isEqualTo("UPDATE \"EMPLOYEES\" \"E\" SET \"EMP_NAME\" = ?, \"EMP_DEPT_ID\" = NULL WHERE \"E\".\"EMP_ID\" = ?");
		assertThat(rendered.params()).containsExactly("Alice", 42);
	}

	@Test
	void compileBindsSetValuesBeforeWhereClause() {
		SqlParameter<Double> salary = SqlParameters.param("salary");
		SqlParameter<Integer> dept = SqlParameters.param("dept");
		SqlParameter<Integer> empId = SqlParameters.param("empId");

		CompiledQuery compiled = SqlQuery.update(TestSchema.EMPLOYEES)
				.set(TestSchema.EMP_SALARY.column(), salary)
				.set(TestSchema.EMP_DEPT_ID.column(), dept)
				.where(TestSchema.EMP_ID.column()).eq(empId)
				.compile();

		assertThat(compiled.sql())
				.isEqualTo("UPDATE \"EMPLOYEES\" \"E\" SET \"EMP_SALARY\" = ?, \"EMP_DEPT_ID\" = ? WHERE \"E\".\"EMP_ID\" = ?");

		SqlAndParams bound = compiled.bind(Map.of("salary", 120_000d, "dept", 9, "empId", 15));
		assertThat(bound.params()).containsExactly(120_000d, 9, 15);
	}

	@Test
	void setRawAppendsFragmentParameters() {
		SqlParameter<Double> bonus = SqlParameters.param("bonus");
		SqlParameter<Integer> empId = SqlParameters.param("empId");

		CompiledQuery compiled = SqlQuery.update(TestSchema.EMPLOYEES)
				.setRaw("\"E\".\"EMP_SALARY\" = \"E\".\"EMP_SALARY\" + ?", bonus)
				.where(TestSchema.EMP_ID.column()).eq(empId)
				.compile();

		assertThat(compiled.sql())
				.isEqualTo("UPDATE \"EMPLOYEES\" \"E\" SET \"E\".\"EMP_SALARY\" = \"E\".\"EMP_SALARY\" + ? WHERE \"E\".\"EMP_ID\" = ?");
		assertThat(compiled.bind(Map.of("bonus", 5.0, "empId", 7)).params()).containsExactly(5.0, 7);
	}

	@Test
	void updateRequiresAtLeastOneAssignment() {
		assertThatThrownBy(() -> SqlQuery.update(TestSchema.EMPLOYEES).where(TestSchema.EMP_ID.column()).eq(1).render())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("set(...)");
	}
}
