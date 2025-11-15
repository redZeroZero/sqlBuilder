package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.InsertQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.Test;

class InsertQueryTest {

	@Test
	void literalValuesRenderPlaceholders() {
		SqlAndParams rendered = SqlQuery.insertInto(TestSchema.EMPLOYEES)
				.columns(TestSchema.EMP_ID.column(), TestSchema.EMP_NAME.column(), TestSchema.EMP_DEPT_ID.column())
				.values(10, "Alice", null)
				.render();

		assertThat(rendered.sql()).isEqualTo(
				"INSERT INTO \"EMPLOYEES\" (\"EMP_ID\", \"EMP_NAME\", \"EMP_DEPT_ID\") VALUES (?, ?, NULL)");
		assertThat(rendered.params()).containsExactly(10, "Alice");
	}

	@Test
	void multipleRowsSupportParameters() {
		SqlParameter<String> name = SqlParameters.param("name");
		SqlParameter<Integer> dept = SqlParameters.param("dept");

		InsertQuery insert = SqlQuery.insertInto(TestSchema.EMPLOYEES)
				.columns(TestSchema.EMP_ID.column(), TestSchema.EMP_NAME.column(), TestSchema.EMP_DEPT_ID.column())
				.values(1, "Alice", 5)
				.values(2, name, dept);

		CompiledQuery compiled = insert.compile();
		assertThat(compiled.sql()).isEqualTo(
				"INSERT INTO \"EMPLOYEES\" (\"EMP_ID\", \"EMP_NAME\", \"EMP_DEPT_ID\") VALUES (?, ?, ?), (?, ?, ?)");

		SqlAndParams bound = compiled.bind(Map.of("name", "Bob", "dept", 7));
		assertThat(bound.params()).containsExactly(1, "Alice", 5, 2, "Bob", 7);
	}

	@Test
	void insertSelectAppendsInnerPlaceholders() {
		SqlParameter<Integer> cutoff = SqlParameters.param("cutoff");
		Query select = SqlQuery.newQuery()
				.select(TestSchema.EMP_ID.column(), TestSchema.EMP_DEPT_ID.column())
				.from(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_DEPT_ID.column()).eq(cutoff)
				.asQuery();

		CompiledQuery compiled = SqlQuery.insertInto(TestSchema.EMPLOYEES)
				.columns(TestSchema.EMP_ID.column(), TestSchema.EMP_DEPT_ID.column())
				.select(select)
				.compile();

		assertThat(compiled.sql()).startsWith("INSERT INTO \"EMPLOYEES\" (\"EMP_ID\", \"EMP_DEPT_ID\") SELECT ");
		SqlAndParams bound = compiled.bind(Map.of("cutoff", 3));
		assertThat(bound.params()).containsExactly(3);
	}

	@Test
	void valuesCountMustMatchColumns() {
		assertThatThrownBy(() -> SqlQuery.insertInto(TestSchema.EMPLOYEES)
				.columns(TestSchema.EMP_ID.column(), TestSchema.EMP_NAME.column())
				.values(1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void selectCannotMixWithValues() {
		Query select = SqlQuery.newQuery().select(TestSchema.EMP_ID.column()).from(TestSchema.EMPLOYEES).asQuery();
		assertThatThrownBy(() -> SqlQuery.insertInto(TestSchema.EMPLOYEES)
				.columns(TestSchema.EMP_ID.column())
				.values(1)
				.select(select))
				.isInstanceOf(IllegalStateException.class);
	}
}
