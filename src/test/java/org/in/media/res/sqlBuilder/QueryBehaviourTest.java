package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;
import org.in.media.res.sqlBuilder.implementation.From;
import org.in.media.res.sqlBuilder.implementation.Query;
import org.in.media.res.sqlBuilder.implementation.Select;
import org.in.media.res.sqlBuilder.implementation.Where;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryBehaviourTest {

	private EmployeeSchema schema;
	private ITable employee;
	private ITable job;

	@BeforeEach
	void setUp() {
		schema = new EmployeeSchema();
		employee = schema.getTableBy(Employee.class);
		job = schema.getTableBy(Job.class);
	}

	@Test
	void fromVarargsIncludesAllTables() {
		Query query = new Query();
		query.from(employee, job);

		String sql = query.transpile();

		assertTrue(sql.contains("Employee"));
		assertTrue(sql.contains("Job"));
	}

	@Test
	void orConnectorReturnsSameQueryInstance() {
		Query query = new Query();
		IColumn firstName = employee.get(Employee.C_FIRST_NAME);

		IColumn lastName = employee.get(Employee.C_LAST_NAME);

		assertSame(query, query.where(firstName).eq("Alice").or(lastName));
		assertSame(query, query.or());
	}

	@Test
	void whereTranspilerSkipsEmptyClauses() {
		Where where = new Where();

		assertEquals("", where.transpile());
	}

	@Test
	void selectResetClearsState() {
		Select select = new Select();
		IColumn firstName = employee.get(Employee.C_FIRST_NAME);

		select.select(firstName);
		select.select(AggregateOperator.MAX, firstName);

		select.reset();

		assertTrue(select.columns().isEmpty());
		assertTrue(select.aggColumns().isEmpty());

		select.select(firstName);
		assertEquals(1, select.columns().size());
	}

	@Test
	void whereOperatorsRequireExistingCondition() {
		Where where = new Where();
		assertThrows(IllegalStateException.class, () -> where.eq("value"));
	}

	@Test
	void fromTranspilerRendersBaseTables() {
		From from = new From();
		from.from(employee);

		assertTrue(from.transpile().contains("Employee"));
	}

	@Test
	void fromTranspilerReturnsEmptyStringWhenNoTables() {
		From from = new From();
		assertEquals("", from.transpile());
	}

	@Test
	void groupByClauseAppearsAfterWhere() {
		Query query = new Query();
		query.select(employee.get(Employee.C_FIRST_NAME)).from(employee).groupBy(employee.get(Employee.C_FIRST_NAME));

		String sql = query.transpile();

		assertTrue(sql.contains(" GROUP BY "));
		assertTrue(sql.contains(employee.get(Employee.C_FIRST_NAME).transpile(false)));
	}

	@Test
	void orderBySupportsAscendingAndDescending() {
		Query query = new Query();
		query.select(employee.get(Employee.C_FIRST_NAME)).from(employee).orderBy(employee.get(Employee.C_LAST_NAME))
				.orderBy(employee.get(Employee.C_ID), SortDirection.DESC);

		String sql = query.transpile();

		assertTrue(sql.contains(" ORDER BY "));
		assertTrue(sql.contains(employee.get(Employee.C_LAST_NAME).transpile(false) + " ASC"));
		assertTrue(sql.contains(employee.get(Employee.C_ID).transpile(false) + " DESC"));
	}

	@Test
	void havingClauseFollowsGroupBy() {
		Query query = new Query();
		query.select(employee.get(Employee.C_FIRST_NAME)).from(employee).groupBy(employee.get(Employee.C_FIRST_NAME))
				.having(employee.get(Employee.C_FIRST_NAME)).eq("Alice");

		String sql = query.transpile();

		int groupByIndex = sql.indexOf(" GROUP BY ");
		int havingIndex = sql.indexOf(" HAVING ");
		assertTrue(groupByIndex > 0 && havingIndex > groupByIndex);
		assertTrue(sql.contains(" HAVING "));
	}

	@Test
	void havingBuilderSupportsAggregates() {
		Query query = new Query();
		query.select(job.get(Job.C_EMPLOYEE_ID)).select(AggregateOperator.AVG, job.get(Job.C_SALARY)).from(job)
				.groupBy(job.get(Job.C_EMPLOYEE_ID)).having(job.get(Job.C_SALARY)).avg(job.get(Job.C_SALARY))
				.supTo(50000);

		String sql = query.transpile();

		assertTrue(sql.contains(" HAVING AVG("));
		assertTrue(sql.contains(job.get(Job.C_SALARY).transpile(false)));
		assertTrue(sql.contains(" > 50000"));
	}

	@Test
	void limitAndOffsetRenderWithOracleSyntax() {
		Query query = new Query();
		query.select(employee.get(Employee.C_FIRST_NAME)).from(employee).orderBy(employee.get(Employee.C_FIRST_NAME))
				.limitAndOffset(10, 5);

		String sql = query.transpile();

		assertTrue(sql.contains(" OFFSET 5 ROWS"));
		assertTrue(sql.contains(" FETCH NEXT 10 ROWS ONLY"));
	}
}
