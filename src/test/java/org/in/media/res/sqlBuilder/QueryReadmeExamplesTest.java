package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;
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
		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.select(Employee.C_LAST_NAME)
				.innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.where(Employee.C_FIRST_NAME).eq("Alice")
				.orderBy(Employee.C_LAST_NAME)
				.limitAndOffset(20, 0)
				.transpile();

		String expected = "SELECT E.FIRST_NAME as firstName, E.LAST_NAME as lastName "
				+ "FROM Employee E INNER JOIN Job J ON E.ID = J.EMPLOYEE_ID "
				+ "WHERE E.FIRST_NAME = 'Alice' ORDER BY E.LAST_NAME ASC "
				+ "OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY";

		assertEquals(expected, sql);
	}

	@Test
	void simpleProjectionExampleSelectsAllEmployeeColumns() {
		String sql = QueryImpl.newQuery()
				.select(employee)
				.transpile();

		String expected = "SELECT E.ID, E.FIRST_NAME as firstName, E.LAST_NAME as lastName, "
				+ "E.MAIL as email, E.PASSWORD as passwd FROM Employee E";

		assertEquals(expected, sql);
	}

	@Test
	void joinWithFilterExampleMatchesReadmeFlow() {
		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME, Job.C_DESCRIPTION)
				.leftJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.where(Job.C_SALARY).supOrEqTo(50000)
				.transpile();

		String expected = "SELECT E.FIRST_NAME as firstName, J.DESCRIPTION as Intitule "
				+ "FROM Employee E LEFT JOIN Job J ON E.ID = J.EMPLOYEE_ID "
				+ "WHERE J.SALARY >= 50000";

		assertEquals(expected, sql);
	}

	@Test
	void aggregationExampleProducesGroupedAverage() {
		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Employee.C_FIRST_NAME)
				.having(Job.C_SALARY).avg(Job.C_SALARY).supTo(60000)
				.orderBy(Employee.C_FIRST_NAME)
				.transpile();

		String expected = "SELECT AVG(J.SALARY), E.FIRST_NAME as firstName FROM Employee E "
				+ "JOIN Job J ON E.ID = J.EMPLOYEE_ID GROUP BY E.FIRST_NAME "
				+ "HAVING AVG(J.SALARY) > 60000 ORDER BY E.FIRST_NAME ASC";

		assertEquals(expected, sql);
	}

	@Test
	void paginationExampleUsesOffsetFetchSyntax() {
		String sql = QueryImpl.newQuery()
				.select(Job.C_DESCRIPTION)
				.from(job)
				.orderBy(Job.C_SALARY, SortDirection.DESC)
				.limitAndOffset(10, 20)
				.transpile();

		String expected = "SELECT J.DESCRIPTION as Intitule FROM Job J "
				+ "ORDER BY J.SALARY DESC OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY";

		assertEquals(expected, sql);
	}

	@Test
	void quickCountAndPrettyPrintExampleMatchesReadme() {
		assertEquals("SELECT COUNT(*)", QueryImpl.countAll().transpile());

		Query printable = QueryImpl.newQuery();
		printable.select(Employee.C_FIRST_NAME);
		printable.from(employee);
		printable.where(Employee.C_FIRST_NAME).eq("Alice");

		String expected = "SELECT E.FIRST_NAME as firstName\n"
				+ "FROM Employee E\n"
				+ "WHERE E.FIRST_NAME = 'Alice'";

		assertEquals(expected, printable.prettyPrint());
	}
}
