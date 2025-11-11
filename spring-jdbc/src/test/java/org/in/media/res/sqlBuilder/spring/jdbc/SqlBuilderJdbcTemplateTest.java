package org.in.media.res.sqlBuilder.spring.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
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
        Query query = SqlQuery.newQuery().asQuery();
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
        Query query = SqlQuery.newQuery().asQuery();
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
}
