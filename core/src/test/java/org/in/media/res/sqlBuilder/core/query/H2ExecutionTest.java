package org.in.media.res.sqlBuilder.core.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.testutil.TestSchema;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Executes representative DSL queries against an in-memory H2 database (PostgreSQL mode)
 * to validate runtime behaviour, not just string rendering.
 */
class H2ExecutionTest {

	private static Connection connection;

	@BeforeAll
	static void startDatabase() throws Exception {
		connection = DriverManager.getConnection(
				"jdbc:h2:mem:sqlbuilder;MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "sa");
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("""
					CREATE TABLE "EMPLOYEES" (
						"EMP_ID" INT PRIMARY KEY,
						"EMP_NAME" VARCHAR(100) NOT NULL,
						"EMP_SALARY" DOUBLE PRECISION NOT NULL,
						"EMP_DEPT_ID" INT NOT NULL,
						"EMP_HIRED_AT" DATE NOT NULL
					);
					""");
			stmt.execute("""
					CREATE TABLE "DEPARTMENTS" (
						"DEPT_ID" INT PRIMARY KEY,
						"DEPT_NAME" VARCHAR(100) NOT NULL
					);
					""");
			stmt.execute("""
					INSERT INTO "DEPARTMENTS" ("DEPT_ID", "DEPT_NAME") VALUES
					(10, 'Engineering'),
					(20, 'Finance');
					""");
			stmt.execute("""
					INSERT INTO "EMPLOYEES" ("EMP_ID", "EMP_NAME", "EMP_SALARY", "EMP_DEPT_ID", "EMP_HIRED_AT") VALUES
					(1, 'Alice', 120000, 10, DATE '2018-01-01'),
					(2, 'Bob', 90000, 10, DATE '2019-06-15'),
					(3, 'Cara', 110000, 20, DATE '2020-03-20');
					""");
		}
	}

	@AfterAll
	static void stopDatabase() throws Exception {
		if (connection != null) {
			connection.close();
		}
	}

	@Test
	void simpleProjectionExecutes() throws Exception {
		Query query = SqlQuery.query()
				.select(TestSchema.EMP_ID)
				.select(TestSchema.EMP_NAME)
				.from(TestSchema.EMPLOYEES)
				.orderBy(TestSchema.EMP_ID);

		List<Map<String, Object>> rows = execute(query.render());
		assertThat(rows).hasSize(3);
		assertThat(rows.getFirst().get("EMP_NAME")).isEqualTo("Alice");
	}

	@Test
	void aggregatesAndHavingExecute() throws Exception {
		Table employees = TestSchema.EMPLOYEES;
		Query query = SqlQuery.query()
				.select(TestSchema.EMP_DEPT_ID)
				.select(AggregateOperator.AVG, TestSchema.EMP_SALARY)
				.from(employees)
				.groupBy(TestSchema.EMP_DEPT_ID)
				.having(TestSchema.EMP_SALARY).avg(TestSchema.EMP_SALARY).supTo(95_000)
				.orderBy(TestSchema.EMP_DEPT_ID);

		List<Map<String, Object>> rows = execute(query.render());
		assertThat(rows).hasSize(2);
		assertThat(rows.get(0).get("EMP_DEPT_ID")).isEqualTo(10);
	}

	@Test
	void optionalFilterBindsNullsAndValues() throws Exception {
		SqlParameter<String> nameFilter = SqlParameters.param("nameFilter", String.class);

		Query base = SqlQuery.newQuery()
				.select(TestSchema.EMP_NAME)
				.from(TestSchema.EMPLOYEES)
				.whereOptionalEquals(TestSchema.EMP_NAME, nameFilter)
				.asQuery();
		CompiledQuery compiled = base.compile();

		Map<String, Object> none = new HashMap<>();
		none.put(nameFilter.name(), null);
		SqlAndParams noFilter = compiled.bind(none);
		List<Map<String, Object>> all = execute(noFilter);
		assertThat(all).hasSize(3);

		SqlAndParams withFilter = compiled.bind(Map.of(nameFilter.name(), "Alice"));
		List<Map<String, Object>> one = execute(withFilter);
		assertThat(one).hasSize(1);
		assertThat(one.getFirst().get("EMP_NAME")).isEqualTo("Alice");
	}

	@Test
	void updateExecutesWithUnqualifiedSetColumns() throws Exception {
		SqlParameter<Double> salaryParam = SqlParameters.param("salary", Double.class);
		SqlParameter<Integer> idParam = SqlParameters.param("id", Integer.class);

		CompiledQuery compiled = SqlQuery.update(TestSchema.EMPLOYEES)
				.set(TestSchema.EMP_SALARY.column(), salaryParam)
				.where(TestSchema.EMP_ID.column()).eq(idParam)
				.compile();

		int updated = executeUpdate(compiled.bind(Map.of("salary", 130_000d, "id", 2)));
		assertThat(updated).isEqualTo(1);

		List<Map<String, Object>> rows = execute(SqlQuery.query()
				.select(TestSchema.EMP_SALARY)
				.from(TestSchema.EMPLOYEES)
				.where(TestSchema.EMP_ID).eq(2)
				.render());
		assertThat(rows).hasSize(1);
		assertThat(((Number) rows.getFirst().get("EMP_SALARY")).doubleValue()).isEqualTo(130_000d);
	}

	private List<Map<String, Object>> execute(SqlAndParams sql) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(sql.sql())) {
			bind(stmt, sql.params());
			try (ResultSet rs = stmt.executeQuery()) {
				List<Map<String, Object>> rows = new ArrayList<>();
				int columnCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					Map<String, Object> row = new HashMap<>();
					for (int i = 1; i <= columnCount; i++) {
						row.put(rs.getMetaData().getColumnLabel(i).toUpperCase(), rs.getObject(i));
					}
					rows.add(row);
				}
				return rows;
			}
		}
	}

	private int executeUpdate(SqlAndParams sql) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(sql.sql())) {
			bind(stmt, sql.params());
			return stmt.executeUpdate();
		}
	}

	private void bind(PreparedStatement stmt, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			stmt.setObject(i + 1, params.get(i));
		}
	}
}
