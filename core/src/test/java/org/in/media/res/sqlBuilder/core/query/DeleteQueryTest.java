package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.DeleteQuery;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class DeleteQueryTest {

	@Test
	void renderIncludesWhereLiterals() {
		SqlAndParams rendered = SqlQuery.deleteFrom(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_ID.column()).eq(99)
				.render();

		assertThat(rendered.sql()).isEqualTo("DELETE FROM \"EMPLOYEES\" \"E\" WHERE \"E\".\"EMP_ID\" = ?");
		assertThat(rendered.params()).containsExactly(99);
	}

	@Test
	void compileBindsParameters() {
		SqlParameter<Integer> dept = SqlParameters.param("dept");
		CompiledQuery compiled = SqlQuery.deleteFrom(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_DEPT_ID.column()).eq(dept)
				.compile();

		assertThat(compiled.sql()).isEqualTo("DELETE FROM \"EMPLOYEES\" \"E\" WHERE \"E\".\"EMP_DEPT_ID\" = ?");
		SqlAndParams bound = compiled.bind(Map.of("dept", 7));
		assertThat(bound.params()).containsExactly(7);
	}

	@Test
	void deleteSupportsOptionalConditions() {
		SqlParameter<Integer> dept = SqlParameters.param("dept");
		DeleteQuery delete = SqlQuery.deleteFrom(TestSchema.EMPLOYEES)
				.whereOptionalEquals(TestSchema.EMP_DEPT_ID.column(), dept);

		CompiledQuery compiled = delete.compile();
		assertThat(compiled.sql())
				.isEqualTo("DELETE FROM \"EMPLOYEES\" \"E\" WHERE (? IS NULL OR \"E\".\"EMP_DEPT_ID\" = ?)");
	}
}
