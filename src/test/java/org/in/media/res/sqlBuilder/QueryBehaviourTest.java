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

		assertSame(query, query.where(Employee.C_FIRST_NAME).eq("Alice").or(Employee.C_LAST_NAME));
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
		select.select(Employee.C_FIRST_NAME);
		select.select(AggregateOperator.MAX, Employee.C_FIRST_NAME);

		select.reset();

		assertTrue(select.columns().isEmpty());
		assertTrue(select.aggColumns().isEmpty());

		select.select(Employee.C_FIRST_NAME);
		assertEquals(1, select.columns().size());
	}

	@Test
	void selectSupportsDescriptorShortcut() {
		Select select = new Select();
		select.select(Employee.C_FIRST_NAME, Employee.C_LAST_NAME);

		String sql = select.transpile();

		assertTrue(sql.contains(employee.get(Employee.C_FIRST_NAME).transpile(false)));
		assertTrue(sql.contains(employee.get(Employee.C_LAST_NAME).transpile(false)));
	}

	@Test
	void querySelectRegistersBaseTableAutomatically() {
		Query query = new Query();
		String sql = query.select(employee).transpile();

		assertTrue(sql.contains(" FROM "));
		assertTrue(sql.contains(employee.getName()));
	}

	@Test
	void querySelectColumnRegistersBaseTableAutomatically() {
		Query query = new Query();
		String sql = query.select(Employee.C_FIRST_NAME).transpile();

		assertTrue(sql.contains(" FROM "));
		assertTrue(sql.contains(employee.getName()));
	}

	@Test
	void joinSupportsDescriptorShortcut() {
		Query query = new Query();
		String sql = query.select(Employee.C_FIRST_NAME).innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID).transpile();

		assertTrue(sql.contains(" JOIN "));
		assertTrue(sql.contains(Employee.C_ID.column().transpile(false)));
		assertTrue(sql.contains(Job.C_EMPLOYEE_ID.column().transpile(false)));
	}

	@Test
	void whereOperatorsRequireExistingCondition() {
		Where where = new Where();
		assertThrows(IllegalStateException.class, () -> where.eq("value"));
	}

	@Test
	void whereSupportsDescriptorShortcut() {
		Query query = new Query();
		query.where(Employee.C_FIRST_NAME).eq("Alice");

		String sql = query.transpile();

		assertTrue(sql.contains(employee.get(Employee.C_FIRST_NAME).transpile(false)));
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
	void fromTranspilerFailsWhenJoinDefinedWithoutBaseTable() {
		From from = new From();
		from.join(job);

		assertThrows(IllegalStateException.class, from::transpile);
	}

	@Test
	void fromTranspilerFailsWhenJoinMissingJoinColumns() {
		From from = new From();
		from.from(employee);
		from.join(job);

		assertThrows(IllegalStateException.class, from::transpile);
	}

	@Test
	void groupByClauseAppearsAfterWhere() {
		Query query = new Query();
		query.select(Employee.C_FIRST_NAME).groupBy(Employee.C_FIRST_NAME);

		String sql = query.transpile();

		assertTrue(sql.contains(" GROUP BY "));
		assertTrue(sql.contains(Employee.C_FIRST_NAME.column().transpile(false)));
	}

	@Test
	void orderBySupportsAscendingAndDescending() {
		Query query = new Query();
		query.select(Employee.C_FIRST_NAME).orderBy(Employee.C_LAST_NAME)
				.orderBy(Employee.C_ID, SortDirection.DESC);

		String sql = query.transpile();

		assertTrue(sql.contains(" ORDER BY "));
		assertTrue(sql.contains(Employee.C_LAST_NAME.column().transpile(false) + " ASC"));
		assertTrue(sql.contains(Employee.C_ID.column().transpile(false) + " DESC"));
	}

	@Test
	void havingClauseFollowsGroupBy() {
		Query query = new Query();
		query.select(Employee.C_FIRST_NAME).groupBy(Employee.C_FIRST_NAME)
				.having(Employee.C_FIRST_NAME).eq("Alice");

		String sql = query.transpile();

		int groupByIndex = sql.indexOf(" GROUP BY ");
		int havingIndex = sql.indexOf(" HAVING ");
		assertTrue(groupByIndex > 0 && havingIndex > groupByIndex);
		assertTrue(sql.contains(" HAVING "));
	}

	@Test
	void havingBuilderSupportsAggregates() {
		Query query = new Query();
		query.select(Job.C_EMPLOYEE_ID).select(AggregateOperator.AVG, Job.C_SALARY)
				.groupBy(Job.C_EMPLOYEE_ID)
				.having(Job.C_SALARY).avg(Job.C_SALARY).supTo(50000);

		String sql = query.transpile();

		assertTrue(sql.contains(" HAVING AVG("));
		assertTrue(sql.contains(Job.C_SALARY.column().transpile(false)));
		assertTrue(sql.contains(" > 50000"));
	}

	@Test
	void limitAndOffsetRenderWithOracleSyntax() {
		Query query = new Query();
		query.select(Employee.C_FIRST_NAME).orderBy(Employee.C_FIRST_NAME)
				.limitAndOffset(10, 5);

		String sql = query.transpile();

		assertTrue(sql.contains(" OFFSET 5 ROWS"));
		assertTrue(sql.contains(" FETCH NEXT 10 ROWS ONLY"));
	}
}
