package org.in.media.res.sqlBuilder.spring.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.DeleteQuery;
import org.in.media.res.sqlBuilder.api.query.InsertQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.UpdateQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class SqlBuilderJdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private SqlBuilderJdbcTemplate sql;
    private Table employees;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:sqlbuilder;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DROP TABLE IF EXISTS EMPLOYEES");
        jdbcTemplate.execute("CREATE TABLE EMPLOYEES (id INT PRIMARY KEY, first_name VARCHAR(64), last_name VARCHAR(64), salary DECIMAL)");
        jdbcTemplate.update("INSERT INTO employees (id, first_name, last_name, salary) VALUES (1, 'Alice', 'Martin', 85000)");
        jdbcTemplate.update("INSERT INTO employees (id, first_name, last_name, salary) VALUES (2, 'Bob', 'Dubois', 92000)");
        sql = new SqlBuilderJdbcTemplate(jdbcTemplate);
        employees = Tables.builder("EMPLOYEES", "e")
                .column("ID")
                .column("FIRST_NAME")
                .column("LAST_NAME")
                .column("SALARY")
                .build();
    }

    @Test
    void queryReturnsRows() {
		Query query = SqlQuery.query();
        query.select(employees.get("FIRST_NAME"))
                .select(employees.get("LAST_NAME"))
                .from(employees)
                .where(employees.get("SALARY")).supOrEqTo(85000)
                .orderBy(employees.get("LAST_NAME"));

        List<String> names = sql.query(query.render(), (rs, rowNum) -> rs.getString("first_name") + " " + rs.getString("last_name"));

        assertThat(names).containsExactlyInAnyOrder("Alice Martin", "Bob Dubois");
    }

    @Test
    void queryForObjectReturnsSingleRow() {
		Query query = SqlQuery.query();
        query.select(employees.get("SALARY"))
                .from(employees)
                .where(employees.get("ID")).eq(2);

        BigDecimal salary = sql.queryForObject(query.render(), (rs, rowNum) -> rs.getBigDecimal("salary"));

        assertThat(salary).isEqualByComparingTo("92000");
    }

	@Test
	void updateExecutesDml() {
		SqlAndParams update = new SqlAndParams("UPDATE employees SET salary = ? WHERE first_name = ?", List.of(95000, "Alice"));
		int updated = sql.update(update);

        assertThat(updated).isEqualTo(1);
		Number salary = jdbcTemplate.queryForObject("SELECT salary FROM employees WHERE first_name = ?", Number.class, "Alice");
		assertThat(salary.intValue()).isEqualTo(95000);
	}

	@Test
	void updateWithCompiledQueryBindsParameters() {
		SqlParameter<BigDecimal> salary = SqlParameters.param("salary");
		SqlParameter<Integer> id = SqlParameters.param("id");

		var compiled = SqlQuery.update(employees)
				.set(employees.get("SALARY"), salary)
				.where(employees.get("ID")).eq(id)
				.compile();

		int updated = sql.update(compiled, Map.of("salary", new BigDecimal("91000"), "id", 2));
		assertThat(updated).isEqualTo(1);
		Number salaryValue = jdbcTemplate.queryForObject("SELECT salary FROM employees WHERE id = ?", Number.class, 2);
		assertThat(salaryValue.intValue()).isEqualTo(91000);
	}

	@Test
	void updateWithBuilderConvenienceRendersOnce() {
		UpdateQuery updateQuery = SqlQuery.update(employees)
				.set(employees.get("LAST_NAME"), "Dupont")
				.where(employees.get("ID")).eq(1);

		int updated = sql.update(updateQuery);
		assertThat(updated).isEqualTo(1);
		String lastName = jdbcTemplate.queryForObject("SELECT last_name FROM employees WHERE id = ?", String.class, 1);
		assertThat(lastName).isEqualTo("Dupont");
	}

	@Test
	void insertWithBuilderAddsRow() {
		InsertQuery insertQuery = SqlQuery.insertInto(employees)
				.columns(employees.get("ID"), employees.get("FIRST_NAME"), employees.get("LAST_NAME"),
						employees.get("SALARY"))
				.values(3, "Cara", "Lopez", new BigDecimal("93000"));

		int inserted = sql.insert(insertQuery);
		assertThat(inserted).isEqualTo(1);
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees WHERE id = ?", Integer.class, 3);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void insertBindsParametersFromMap() {
		SqlParameter<Integer> id = SqlParameters.param("id");
		SqlParameter<String> first = SqlParameters.param("first");
		SqlParameter<String> last = SqlParameters.param("last");
		SqlParameter<BigDecimal> salary = SqlParameters.param("salary");

		InsertQuery insert = SqlQuery.insertInto(employees)
				.columns(employees.get("ID"), employees.get("FIRST_NAME"), employees.get("LAST_NAME"),
						employees.get("SALARY"))
				.values(id, first, last, salary);

		Map<String, Object> params = Map.of("id", 4, "first", "Dan", "last", "Klein", "salary",
				new BigDecimal("87000"));
		int inserted = sql.insert(insert, params);
		assertThat(inserted).isEqualTo(1);
		Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees WHERE id = ?", Integer.class, 4);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void deleteRemovesRows() {
		DeleteQuery delete = SqlQuery.deleteFrom(employees)
				.where(employees.get("FIRST_NAME")).eq("Bob");

		int removed = sql.delete(delete);
		assertThat(removed).isEqualTo(1);
		Integer remaining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees WHERE first_name = ?",
				Integer.class, "Bob");
		assertThat(remaining).isZero();
	}

	@Test
	void deleteBindsParameters() {
		SqlParameter<Integer> id = SqlParameters.param("id");
		DeleteQuery delete = SqlQuery.deleteFrom(employees)
				.where(employees.get("ID")).eq(id);

		int removed = sql.delete(delete, Map.of("id", 1));
		assertThat(removed).isEqualTo(1);
		Integer remaining = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees WHERE id = ?", Integer.class, 1);
		assertThat(remaining).isZero();
	}
}
