package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryReadmeExamplesTest {

	private EmployeeSchema schema;
	private Table employee;
	private Table job;

	@BeforeEach
	void setUp() {
		schema = new EmployeeSchema();
		employee = schema.getTableBy(Employee.class);
		job = schema.getTableBy(Job.class);
	}

	@Test
	void gettingStartedExampleMatchesExpectedSql() {
		String sql = SqlQuery.newQuery()
				.select(Employee.C_FIRST_NAME)
				.select(Employee.C_LAST_NAME)
				.from(employee)
				.innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.where(Employee.C_FIRST_NAME).eq("Alice")
				.orderBy(Employee.C_LAST_NAME)
				.limitAndOffset(20, 0)
				.asQuery()
				.render().sql();

		String expected = "SELECT \"E\".\"FIRST_NAME\" as \"firstName\", \"E\".\"LAST_NAME\" as \"lastName\" "
				+ "FROM \"Employee\" \"E\" INNER JOIN \"Job\" \"J\" ON \"E\".\"ID\" = \"J\".\"EMPLOYEE_ID\" "
				+ "WHERE \"E\".\"FIRST_NAME\" = ? ORDER BY \"E\".\"LAST_NAME\" ASC "
				+ "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

		assertEquals(expected, sql);
	}

	@Test
	void simpleProjectionExampleSelectsAllEmployeeColumns() {
		String sql = SqlQuery.newQuery()
				.select(employee)
				.asQuery()
				.render().sql();

		String expected = "SELECT \"E\".\"ID\", \"E\".\"FIRST_NAME\" as \"firstName\", \"E\".\"LAST_NAME\" as \"lastName\", "
				+ "\"E\".\"MAIL\" as \"email\", \"E\".\"PASSWORD\" as \"passwd\" FROM \"Employee\" \"E\"";

		assertEquals(expected, sql);
	}

	@Test
	void joinWithFilterExampleMatchesReadmeFlow() {
		String sql = SqlQuery.newQuery()
				.select(Employee.C_FIRST_NAME, Job.C_DESCRIPTION)
				.from(employee)
				.leftJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.where(Job.C_SALARY).supOrEqTo(50000)
				.asQuery()
				.render().sql();

		String expected = "SELECT \"E\".\"FIRST_NAME\" as \"firstName\", \"J\".\"DESCRIPTION\" as \"Intitule\" "
				+ "FROM \"Employee\" \"E\" LEFT JOIN \"Job\" \"J\" ON \"E\".\"ID\" = \"J\".\"EMPLOYEE_ID\" "
				+ "WHERE \"J\".\"SALARY\" >= ?";

		assertEquals(expected, sql);
	}

	@Test
	void aggregationExampleProducesGroupedAverage() {
		Query aggregate = SqlQuery.query();
		aggregate.select(Employee.C_FIRST_NAME)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Employee.C_FIRST_NAME);
		aggregate.having(Job.C_SALARY).avg(Job.C_SALARY).supTo(60000);
		aggregate.orderBy(Employee.C_FIRST_NAME);

		String sql = aggregate.render().sql();

		String expected = "SELECT \"E\".\"FIRST_NAME\" as \"firstName\", AVG(\"J\".\"SALARY\") FROM \"Employee\" \"E\" "
				+ "JOIN \"Job\" \"J\" ON \"E\".\"ID\" = \"J\".\"EMPLOYEE_ID\" GROUP BY \"E\".\"FIRST_NAME\" "
				+ "HAVING AVG(\"J\".\"SALARY\") > ? ORDER BY \"E\".\"FIRST_NAME\" ASC";

		assertEquals(expected, sql);
	}

	@Test
	void paginationExampleUsesOffsetFetchSyntax() {
		String sql = SqlQuery.newQuery()
				.select(Job.C_DESCRIPTION)
				.from(job)
				.orderBy(Job.C_SALARY, SortDirection.DESC)
				.limitAndOffset(10, 20)
				.asQuery()
				.render().sql();

		String expected = "SELECT \"J\".\"DESCRIPTION\" as \"Intitule\" FROM \"Job\" \"J\" "
				+ "ORDER BY \"J\".\"SALARY\" DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

		assertEquals(expected, sql);
	}

	@Test
	void quickCountAndPrettyPrintExampleMatchesReadme() {
		assertEquals("SELECT COUNT(*)", SqlQuery.countAll().asQuery().render().sql());

		Query printable = SqlQuery.query();
		printable.select(Employee.C_FIRST_NAME);
		printable.from(employee);
		printable.where(Employee.C_FIRST_NAME).eq("Alice");

			String expected = "SELECT \"E\".\"FIRST_NAME\" as \"firstName\"\n"
					+ "FROM \"Employee\" \"E\"\n"
					+ "WHERE \"E\".\"FIRST_NAME\" = ?";

			String actual = Arrays.stream(printable.prettyPrint().replace("\r", "").split("\n"))
					.map(String::stripTrailing)
                    .collect(Collectors.joining("\n"))
                    .strip();
            if (Boolean.getBoolean("sqlbuilder.debug.pretty")) {
                System.out.println("ACTUAL_PRETTY>>>" + actual.replace("\n", "\\n") + "<<<");
            }
            assertEquals(expected, actual);
        }
}
