package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.in.media.res.sqlBuilder.api.query.QueryColumns;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;
import org.in.media.res.sqlBuilder.example.Customer;
import org.in.media.res.sqlBuilder.example.CustomerColumns;
import org.in.media.res.sqlBuilder.example.OrderHeader;
import org.in.media.res.sqlBuilder.example.OrderLine;
import org.in.media.res.sqlBuilder.example.Product;
import org.in.media.res.sqlBuilder.example.Payment;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.QueryHelper;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.core.model.ColumnRef;
import org.in.media.res.sqlBuilder.core.query.FromImpl;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;
import org.in.media.res.sqlBuilder.core.query.SelectImpl;
import org.in.media.res.sqlBuilder.core.query.WhereImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryBehaviourTest {

	private EmployeeSchema schema;
	private Table employee;
	private Table job;
	private Table customer;
	private Table orderHeader;
	private Table orderLine;
	private Table product;
	private Table payment;

	@BeforeEach
	void setUp() {
		schema = new EmployeeSchema();
		employee = schema.getTableBy(Employee.class);
		job = schema.getTableBy(Job.class);
		customer = schema.getTableBy(Customer.class);
		orderHeader = schema.getTableBy(OrderHeader.class);
		orderLine = schema.getTableBy(OrderLine.class);
		product = schema.getTableBy(Product.class);
		payment = schema.getTableBy(Payment.class);
	}

	@Test
	void schemaFacetsExposeGeneratedColumns() {
		CustomerColumns columns = schema.facets().columns(Customer.class, CustomerColumns.class);
		ColumnRef<Long> id = columns.ID();
		assertEquals(Long.class, id.type());
		assertEquals(customer.getName(), id.column().table().getName());

		String sql = SqlQuery.newQuery()
				.select(columns.ID(), columns.FIRST_NAME())
				.from(customer)
				.where(columns.LAST_NAME()).like("%son")
				.transpile();

		assertTrue(sql.contains("Customer"));
		assertTrue(sql.contains("WHERE"));
	}

	@Test
	void queryColumnsBundlesTableAndColumns() {
		QueryColumns<CustomerColumns> helper = QueryColumns.of(schema, CustomerColumns.class);

		assertEquals(customer, helper.table());

		String sql = SqlQuery.newQuery()
				.select(helper.columns().ID())
				.from(helper.table())
				.like(helper.columns().LAST_NAME(), "%son")
				.transpile();

		assertTrue(sql.contains(helper.table().getName()));
		assertTrue(sql.contains("LIKE"));
	}

	@Test
	void fromVarargsIncludesAllTables() {
		Query query = QueryImpl.newQuery();
		query.from(employee, job);

		String sql = query.transpile();

		assertTrue(sql.contains("Employee"));
		assertTrue(sql.contains("Job"));
	}

	@Test
	void orConnectorReturnsSameQueryInstance() {
		Query query = QueryImpl.newQuery();

		assertSame(query, query.where(Employee.C_FIRST_NAME).eq("Alice").or(Employee.C_LAST_NAME));
		assertSame(query, query.or());
	}

	@Test
	void whereTranspilerSkipsEmptyClauses() {
		Where where = new WhereImpl();

		assertEquals("", where.transpile());
	}

	@Test
	void selectResetClearsState() {
		Select select = new SelectImpl();
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
		Select select = new SelectImpl();
		select.select(Employee.C_FIRST_NAME, Employee.C_LAST_NAME);

		String sql = select.transpile();

		assertTrue(sql.contains(employee.get(Employee.C_FIRST_NAME).transpile(false)));
		assertTrue(sql.contains(employee.get(Employee.C_LAST_NAME).transpile(false)));
	}

	@Test
	void querySelectRegistersBaseTableAutomatically() {
		Query query = QueryImpl.newQuery();
		String sql = query.select(employee).transpile();

		assertTrue(sql.contains(" FROM "));
		assertTrue(sql.contains(employee.getName()));
	}

	@Test
	void querySelectColumnRegistersBaseTableAutomatically() {
		Query query = QueryImpl.newQuery();
		String sql = query.select(Employee.C_FIRST_NAME).transpile();

		assertTrue(sql.contains(" FROM "));
		assertTrue(sql.contains(employee.getName()));
	}

	@Test
	void joinSupportsDescriptorShortcut() {
		Query query = QueryImpl.newQuery();
		String sql = query.select(Employee.C_FIRST_NAME).innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID).transpile();

		assertTrue(sql.contains(" JOIN "));
		assertTrue(sql.contains(Employee.C_ID.column().transpile(false)));
		assertTrue(sql.contains(Job.C_EMPLOYEE_ID.column().transpile(false)));
	}

	@Test
	void crossJoinProducesCrossJoinKeyword() {
		String sql = QueryImpl.newQuery()
				.select(employee)
				.crossJoin(job)
				.transpile();

		assertTrue(sql.contains(" CROSS JOIN "));
		assertFalse(sql.contains(" ON "));
	}

	@Test
	void fullOuterJoinRendersKeywordAndRequiresOnClause() {
		String sql = QueryImpl.newQuery()
				.select(employee)
				.fullOuterJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.transpile();

		assertTrue(sql.contains(" FULL OUTER JOIN "));
		assertTrue(sql.contains(" ON "));
	}

	@Test
	void fromSubqueryCreatesDerivedTableWithAlias() {
		Query salarySummary = QueryImpl.newQuery()
				.select(Employee.C_ID)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Employee.C_ID);

		var salaryView = QueryImpl.toTable(salarySummary);

		Query outer = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.join(salaryView).on(Employee.C_ID, salaryView.get("ID"))
				.where(salaryView.get("AVG_pay")).supOrEqTo(60000);

		String sql = outer.transpile();

		assertTrue(sql.contains("(SELECT"));
		assertTrue(sql.contains(") " + salaryView.tableName()));
		assertNotNull(salaryView.get("AVG_pay"));
		assertTrue(sql.contains(salaryView.tableName() + "." + salaryView.get("AVG_pay").getName()));
	}

	@Test
	void whereInSubqueryTranslatesToInClause() {
		Query highSalaryIds = QueryImpl.newQuery()
				.select(Job.C_EMPLOYEE_ID)
				.from(job)
				.where(Job.C_SALARY).supOrEqTo(60000);

		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.where(Employee.C_ID).in(highSalaryIds)
				.transpile();

		assertTrue(sql.contains(" IN ("));
		assertTrue(sql.contains("SELECT J.EMPLOYEE_ID"));
	}

	@Test
	void whereScalarSubqueryComparison() {
		Query averageSalary = QueryImpl.newQuery()
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(job);

		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.where(Employee.C_ID).eq(averageSalary)
				.transpile();

		assertTrue(sql.contains(" = (SELECT AVG"));
	}

	@Test
	void whereExistsSubqueryAppendsExistsClause() {
		Query anyJob = QueryImpl.newQuery()
				.select(Job.C_ID)
				.from(job);

		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.exists(anyJob)
				.transpile();

		assertTrue(sql.contains(" WHERE EXISTS ("));
	}

	@Test
	void scalarSubqueryRequiresSingleColumn() {
		Query invalid = QueryImpl.newQuery()
				.select(Employee.C_ID)
				.select(Employee.C_FIRST_NAME)
				.from(employee);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> QueryImpl.newQuery().select(Employee.C_FIRST_NAME)
						.from(employee)
						.where(Employee.C_ID).eq(invalid));
		assertTrue(ex.getMessage().contains("expected 1 column"));
	}

	@Test
	void existsSubqueryMustProjectColumns() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> QueryImpl.newQuery().select(Employee.C_FIRST_NAME)
						.from(employee)
						.exists(QueryImpl.newQuery()));
		assertTrue(ex.getMessage().contains("subquery selects no columns"));
	}

	@Test
	void whereOperatorsRequireExistingCondition() {
		Where where = new WhereImpl();
		assertThrows(IllegalStateException.class, () -> where.eq("value"));
	}

	@Test
	void whereSupportsDescriptorShortcut() {
		Query query = QueryImpl.newQuery();
		query.where(Employee.C_FIRST_NAME).eq("Alice");

		String sql = query.transpile();

		assertTrue(sql.contains(employee.get(Employee.C_FIRST_NAME).transpile(false)));
	}

	@Test
	void groupedConditionsApplyParentheses() {
		var nameGroup = QueryHelper.group()
				.where(Employee.C_FIRST_NAME).eq("Alice")
				.orGroup()
					.where(Employee.C_LAST_NAME).eq("Smith")
				.endGroup();

		Query query = QueryImpl.newQuery()
				.select(employee)
				.from(employee)
				.where(nameGroup);

		query.and(Employee.C_ID).eq(42);

		String sql = query.transpile();

		assertTrue(sql.contains("(E.FIRST_NAME = 'Alice' OR (E.LAST_NAME = 'Smith'))"), () -> sql);
		assertTrue(sql.contains("AND E.ID = 42"), () -> sql);
	}

	@Test
	void likeOperatorFormatsStringLiteral() {
		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.where(Employee.C_FIRST_NAME).like("%ice%")
				.transpile();

		assertTrue(sql.contains(" LIKE '\\%ice\\%' ESCAPE '\\'"), () -> sql);
	}

	@Test
	void groupedConditionsChainWithAndOperators() {
		var stateGroup = QueryHelper.group()
				.where(Employee.C_LAST_NAME).eq("Miller")
				.or(Employee.C_LAST_NAME).eq("Moore");

		var salaryGroup = QueryHelper.group()
				.where(Job.C_SALARY).supOrEqTo(120_000)
				.orGroup()
					.where(Job.C_SALARY).between(80_000, 90_000)
				.endGroup();

		Query query = QueryImpl.newQuery()
				.select(employee)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID);

		query.where(stateGroup);
		query.and(salaryGroup);

		String sql = query.transpile();

		assertTrue(sql.contains("(E.LAST_NAME = 'Miller' OR E.LAST_NAME = 'Moore')"), () -> sql);
		assertTrue(sql.contains("AND (J.SALARY >= 120000 OR (J.SALARY BETWEEN 80000 AND 90000))"), () -> sql);
	}

	@Test
	void groupedConditionsRespectHavingContext() {
		var highAverage = QueryHelper.group()
				.where(Job.C_SALARY).avg(Job.C_SALARY).supTo(60_000);

		var fallbackAverage = QueryHelper.group()
				.where(Job.C_SALARY).avg(Job.C_SALARY).supOrEqTo(55_000);

		Query query = QueryImpl.newQuery()
				.select(Employee.C_ID)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Employee.C_ID);

		query.having(highAverage);
		query.and(fallbackAverage);

		String sql = query.transpile();

		assertTrue(sql.contains("HAVING (AVG(J.SALARY) > 60000) AND (AVG(J.SALARY) >= 55000)"), () -> sql);
		assertFalse(sql.contains("WHERE AVG("));
	}

	@Test
	void customerPaymentAggregations() {
		Query query = QueryImpl.newQuery()
				.select(customer.get("ID"))
				.select(customer.get("FIRST_NAME"))
				.select(AggregateOperator.SUM, payment.get("AMOUNT"))
				.from(customer)
				.join(orderHeader).on(customer.get("ID"), orderHeader.get("CUSTOMER_ID"))
				.join(payment).on(payment.get("ORDER_ID"), orderHeader.get("ID"))
				.groupBy(customer.get("ID"), customer.get("FIRST_NAME"))
				.orderBy(customer.get("FIRST_NAME"));

		String sql = query.transpile();

		assertTrue(sql.contains("FROM Customer"));
		assertTrue(sql.contains("JOIN Orders"));
		assertTrue(sql.contains("JOIN Payment"));
		assertTrue(sql.contains("SUM(PAY.AMOUNT)"));
	}

	@Test
	void orderLineJoinProductProducesJoinClause() {
		Query query = QueryImpl.newQuery()
				.select(product.get("NAME"))
				.select(orderLine.get("QUANTITY"))
				.from(orderLine)
				.join(product).on(orderLine.get("PRODUCT_ID"), product.get("ID"));

		String sql = query.transpile();

		assertTrue(sql.contains("JOIN Product"));
		assertTrue(sql.contains("OL.PRODUCT_ID = P.ID"));
	}

	@Test
	void customersWithAverageOrderValueAboveThreshold() {
		Query query = QueryImpl.newQuery()
				.select(customer.get("ID"))
				.select(customer.get("FIRST_NAME"))
				.from(customer)
				.join(orderHeader).on(customer.get("ID"), orderHeader.get("CUSTOMER_ID"))
				.groupBy(customer.get("ID"), customer.get("FIRST_NAME"))
				.having(orderHeader.get("TOTAL")).avg(orderHeader.get("TOTAL"))
				.supOrEqTo(new java.math.BigDecimal("500.00"));

		String sql = query.transpile();

		assertTrue(sql.contains("AVG(O.TOTAL) >="));
		assertTrue(sql.contains("500"));
	}

	@Test
	void betweenOperatorProducesRangeClause() {
		String sql = QueryImpl.newQuery()
				.select(Employee.C_ID)
				.where(Employee.C_ID).between(1, 10)
				.transpile();

		assertTrue(sql.contains(" BETWEEN 1 AND 10"));
	}

	@Test
	void isNullOperatorOmitsParentheses() {
		String sql = QueryImpl.newQuery()
				.select(Employee.C_LAST_NAME)
				.where(Employee.C_LAST_NAME).isNull()
				.transpile();

		assertTrue(sql.contains(" IS NULL"));
		assertFalse(sql.contains(" IS NULL()"));
	}

	@Test
	void notInOperatorRendersNotInClause() {
		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.where(Employee.C_FIRST_NAME).notIn("Alice", "Bob")
				.transpile();

		assertTrue(sql.contains(" NOT IN ('Alice', 'Bob')"));
	}

	@Test
	void fromTranspilerRendersBaseTables() {
		From from = new FromImpl();
		from.from(employee);

		assertTrue(from.transpile().contains("Employee"));
	}

	@Test
	void fromTranspilerReturnsEmptyStringWhenNoTables() {
		From from = new FromImpl();
		assertEquals("", from.transpile());
	}

	@Test
	void fromTranspilerFailsWhenJoinDefinedWithoutBaseTable() {
		From from = new FromImpl();
		from.join(job);

		assertThrows(IllegalStateException.class, from::transpile);
	}

	@Test
	void fromTranspilerFailsWhenJoinMissingJoinColumns() {
		From from = new FromImpl();
		from.from(employee);
		from.join(job);

		assertThrows(IllegalStateException.class, from::transpile);
	}

	@Test
	void groupByClauseAppearsAfterWhere() {
		Query query = QueryImpl.newQuery();
		query.select(Employee.C_FIRST_NAME).groupBy(Employee.C_FIRST_NAME);

		String sql = query.transpile();

		assertTrue(sql.contains(" GROUP BY "));
		assertTrue(sql.contains(Employee.C_FIRST_NAME.column().transpile(false)));
	}

	@Test
	void orderBySupportsAscendingAndDescending() {
		Query query = QueryImpl.newQuery();
		query.select(Employee.C_FIRST_NAME).orderBy(Employee.C_LAST_NAME)
				.orderBy(Employee.C_ID, SortDirection.DESC);

		String sql = query.transpile();

		assertTrue(sql.contains(" ORDER BY "));
		assertTrue(sql.contains(Employee.C_LAST_NAME.column().transpile(false) + " ASC"));
		assertTrue(sql.contains(Employee.C_ID.column().transpile(false) + " DESC"));
	}

	@Test
	void havingClauseFollowsGroupBy() {
		Query query = QueryImpl.newQuery();
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
		Query query = QueryImpl.newQuery();
		query.select(Job.C_EMPLOYEE_ID).select(AggregateOperator.AVG, Job.C_SALARY)
				.groupBy(Job.C_EMPLOYEE_ID)
				.having(Job.C_SALARY).avg(Job.C_SALARY).supTo(50000);

		String sql = query.transpile();

		assertTrue(sql.contains(" HAVING AVG("));
		assertTrue(sql.contains(Job.C_SALARY.column().transpile(false)));
		assertTrue(sql.contains(" > 50000"));
	}

	@Test
	void havingBetweenSupportsNumericRange() {
		Query query = QueryImpl.newQuery();
		query.select(Job.C_EMPLOYEE_ID)
				.select(AggregateOperator.SUM, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Job.C_EMPLOYEE_ID)
				.having(Job.C_SALARY).between(50000, 100000);

		String sql = query.transpile();

		assertTrue(sql.contains(" HAVING "));
		assertTrue(sql.contains(" BETWEEN 50000 AND 100000"));
	}

	@Test
	void limitAndOffsetRenderWithOracleSyntax() {
		Query query = QueryImpl.newQuery();
		query.select(Employee.C_FIRST_NAME).orderBy(Employee.C_FIRST_NAME)
				.limitAndOffset(10, 5);

		String sql = query.transpile();

		assertTrue(sql.contains(" OFFSET 5 ROWS"));
		assertTrue(sql.contains(" FETCH NEXT 10 ROWS ONLY"));
	}

	@Test
	void selectTranspilerKeepsAggregateFormatting() {
		String sql = QueryImpl.newQuery().select(AggregateOperator.MAX, Employee.C_FIRST_NAME)
				.select(Employee.C_LAST_NAME).transpile();

		assertTrue(sql.startsWith("SELECT MAX("));
		assertTrue(sql.contains("), "));
		assertTrue(sql.contains(Employee.C_LAST_NAME.column().transpile(false)));
	}

	@Test
	void countAllProducesCountStar() {
		String sql = QueryImpl.countAll().transpile();
		assertTrue(sql.startsWith("SELECT COUNT(*)"));
	}

	@Test
	void distinctSelectsRenderDistinctKeyword() {
		String sql = QueryImpl.newQuery()
				.distinct()
				.select(Employee.C_FIRST_NAME)
				.transpile();

		assertTrue(sql.startsWith("SELECT DISTINCT "));
		assertTrue(sql.contains(Employee.C_FIRST_NAME.column().transpile(false)));
	}

	@Test
	void countColumnRegistersTable() {
		Query query = QueryImpl.newQuery().count(Employee.C_ID);
		String sql = query.transpile();
		assertTrue(sql.contains("COUNT(" + Employee.C_ID.column().transpile(false) + ")"));
		assertTrue(sql.contains(" FROM "));
	}

	@Test
	void prettyPrintBreaksClausesAcrossLines() {
		Query query = QueryImpl.newQuery();
		query.select(Employee.C_FIRST_NAME);
		query.from(employee);
		query.where(Employee.C_FIRST_NAME);
		query.eq("Alice");
		query.orderBy(Employee.C_FIRST_NAME);

		String pretty = query.prettyPrint();
		assertTrue(pretty.contains("\nFROM "));
		assertTrue(pretty.contains("\nWHERE "));
		assertTrue(pretty.contains("\nORDER BY "));
	}

	@Test
	void unionCombinesQueries() {
		Query left = QueryImpl.newQuery().select(employee);
		Query right = QueryImpl.newQuery().select(job);

		String sql = left.union(right).transpile();

		String expected = "SELECT E.ID, E.FIRST_NAME as firstName, E.LAST_NAME as lastName, "
				+ "E.MAIL as email, E.PASSWORD as passwd FROM Employee E UNION ("
				+ "SELECT J.ID, J.SALARY as pay, J.DESCRIPTION as Intitule, J.EMPLOYEE_ID as employeeId FROM Job J)";
		assertEquals(expected, sql);
	}

	@Test
	void unionAllKeepsDuplicates() {
		Query base = QueryImpl.newQuery().select(Employee.C_FIRST_NAME);
		Query other = QueryImpl.newQuery().select(Employee.C_FIRST_NAME);

		String sql = base.unionAll(other).transpile();

		assertTrue(sql.contains("UNION ALL"));
	}

	@Test
	void intersectProducesIntersection() {
		Query left = QueryImpl.newQuery().select(Employee.C_ID);
		Query right = QueryImpl.newQuery().select(Job.C_EMPLOYEE_ID);

		String sql = left.intersect(right).transpile();
		assertTrue(sql.contains("INTERSECT"));
	}

	@Test
	void exceptUsesMinusForOracle() {
		Query left = QueryImpl.newQuery().select(employee);
		Query right = QueryImpl.newQuery().select(job);

		String sql = left.except(right).transpile();
		assertTrue(sql.contains("MINUS"));
	}

	@Test
	void exceptAllThrowsUnsupported() {
		Query left = QueryImpl.newQuery().select(employee);
		Query right = QueryImpl.newQuery().select(job);

		assertThrows(UnsupportedOperationException.class, () -> left.exceptAll(right).transpile());
	}
}
