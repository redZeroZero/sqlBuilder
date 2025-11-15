package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.Dialects;
import org.in.media.res.sqlBuilder.api.query.QueryColumns;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlFormatter;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;
import org.in.media.res.sqlBuilder.example.Customer;
import org.in.media.res.sqlBuilder.example.CustomerColumns;
import org.in.media.res.sqlBuilder.example.OrderHeader;
import org.in.media.res.sqlBuilder.example.OrderHeaderColumns;
import org.in.media.res.sqlBuilder.example.OrderLine;
import org.in.media.res.sqlBuilder.example.OrderLineColumns;
import org.in.media.res.sqlBuilder.example.Payment;
import org.in.media.res.sqlBuilder.example.PaymentColumns;
import org.in.media.res.sqlBuilder.example.Product;
import org.in.media.res.sqlBuilder.example.ProductColumns;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.QueryHelper;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
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
	private CustomerColumns customerColumns;
	private OrderHeaderColumns orderHeaderColumns;
	private OrderLineColumns orderLineColumns;
	private ProductColumns productColumns;
	private PaymentColumns paymentColumns;

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
		customerColumns = schema.facets().columns(Customer.class, CustomerColumns.class);
		orderHeaderColumns = schema.facets().columns(OrderHeader.class, OrderHeaderColumns.class);
		orderLineColumns = schema.facets().columns(OrderLine.class, OrderLineColumns.class);
		productColumns = schema.facets().columns(Product.class, ProductColumns.class);
		paymentColumns = schema.facets().columns(Payment.class, PaymentColumns.class);
	}

	private String quoted(String identifier) {
		return "\"" + identifier + "\"";
	}

	private String qualified(String alias, String column) {
		return quoted(alias) + "." + quoted(column);
	}

	private String tableRef(Table table) {
		return table.hasAlias() ? table.getAlias() : table.getName();
	}

	@Test
	void schemaFacetsExposeGeneratedColumns() {
		CustomerColumns columns = schema.facets().columns(Customer.class, CustomerColumns.class);
		ColumnRef<Long> id = columns.ID();
		assertEquals(Long.class, id.type());
		assertEquals(customer.getName(), id.column().table().getName());

		String sql = SqlQuery.query()
				.select(columns.ID(), columns.FIRST_NAME())
				.from(customer)
				.where(columns.LAST_NAME()).like("%son")
				.transpile();

		assertTrue(sql.contains(quoted("Customer")));
		assertTrue(sql.contains("WHERE"));
	}

	@Test
	void schemaLevelDialectControlsQuoting() {
		DialectsTestDialect bracketDialect = new DialectsTestDialect();
		EmployeeSchema customSchema = new EmployeeSchema();
		customSchema.setDialect(bracketDialect);

		Query query = SqlQuery.newQuery(customSchema).asQuery()
				.select(Employee.C_FIRST_NAME)
				.from(employee);

		SqlAndParams rendered = query.limitAndOffset(5, 1).render();

		assertTrue(rendered.sql().contains("[Employee]"));
		assertTrue(rendered.sql().contains(" LIMIT ? OFFSET ?"));
		assertEquals(List.of(5L, 1L), rendered.params());
	}

	@Test
	void hintsAppearAfterSelectKeyword() {
		Query hinted = SqlQuery.query();
		hinted.hint("/*+ INDEX(E EMP_ID_IDX) */")
				.select(Employee.C_FIRST_NAME)
				.from(employee);

		SqlAndParams rendered = hinted.render();

		assertTrue(rendered.sql().startsWith("SELECT /*+ INDEX(E EMP_ID_IDX) */"));
	}

	@Test
	void postgresDialectUsesLimitBeforeOffset() {
		Query pgQuery = SqlQuery.newQuery(Dialects.postgres()).asQuery();
		pgQuery.select(Employee.C_FIRST_NAME)
				.from(employee);

		SqlAndParams rendered = pgQuery.limitAndOffset(25, 5).render();

		assertTrue(rendered.sql().contains(" LIMIT ? OFFSET ?"));
		assertEquals(List.of(25L, 5L), rendered.params());
	}

	@Test
	void queryColumnsBundlesTableAndColumns() {
		QueryColumns<CustomerColumns> helper = QueryColumns.of(schema, CustomerColumns.class);

		assertEquals(customer, helper.table());

		String sql = SqlQuery.query()
				.select(helper.columns().ID())
				.from(helper.table())
				.like(helper.columns().LAST_NAME(), "%son")
				.transpile();

		assertTrue(sql.contains(quoted(tableRef(helper.table()))));
		assertTrue(sql.contains("LIKE"));
	}

	@Test
	void fromVarargsIncludesAllTables() {
		Query query = SqlQuery.query();
		query.from(employee, job);

		String sql = query.transpile();

		assertTrue(sql.contains(quoted(tableRef(employee))));
		assertTrue(sql.contains(quoted(tableRef(job))));
	}

	@Test
	void orConnectorReturnsSameQueryInstance() {
		Query query = SqlQuery.query();

		assertSame(query, query.where(Employee.C_FIRST_NAME).eq("Alice").or(Employee.C_LAST_NAME));
		assertSame(query, query.or());
	}

	@Test
	void whereTranspilerSkipsEmptyClauses() {
		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME);
		String sql = query.transpile();
		assertFalse(sql.contains(" WHERE "));
	}

	@Test
	void compiledQueriesBindNamedParameters() {
		SqlParameter<Integer> minSalary = SqlParameters.param("minSalary");
		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.where(Job.C_SALARY).supOrEqTo(minSalary);

		CompiledQuery template = query.compile();

		SqlAndParams run = template.bind(Map.of("minSalary", 80_000));
		assertTrue(run.sql().contains(qualified(tableRef(job), "SALARY") + " >= ?"));
		assertEquals(List.of(80_000), run.params());
	}

	@Test
	void selectResetClearsState() {
		Query select = SqlQuery.query();
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
		Query select = SqlQuery.query();
		select.select(Employee.C_FIRST_NAME, Employee.C_LAST_NAME);

		String sql = select.transpile();

		assertTrue(sql.contains(employee.get(Employee.C_FIRST_NAME).transpile(false)));
		assertTrue(sql.contains(employee.get(Employee.C_LAST_NAME).transpile(false)));
	}

	@Test
	void querySelectRegistersBaseTableAutomatically() {
		Query query = SqlQuery.query();
		String sql = query.select(employee).transpile();

		assertTrue(sql.contains(" FROM "));
		assertTrue(sql.contains(employee.getName()));
	}

	@Test
	void querySelectColumnRegistersBaseTableAutomatically() {
		Query query = SqlQuery.query();
		String sql = query.select(Employee.C_FIRST_NAME).transpile();

		assertTrue(sql.contains(" FROM "));
		assertTrue(sql.contains(employee.getName()));
	}

	@Test
	void joinSupportsDescriptorShortcut() {
		Query query = SqlQuery.query();
		String sql = query.select(Employee.C_FIRST_NAME).innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID).transpile();

		assertTrue(sql.contains(" JOIN "));
		assertTrue(sql.contains(Employee.C_ID.column().transpile(false)));
		assertTrue(sql.contains(Job.C_EMPLOYEE_ID.column().transpile(false)));
	}

	@Test
	void crossJoinProducesCrossJoinKeyword() {
		String sql = SqlQuery.query()
				.select(employee)
				.crossJoin(job)
				.transpile();

		assertTrue(sql.contains(" CROSS JOIN "));
		assertFalse(sql.contains(" ON "));
	}

	@Test
	void fullOuterJoinRendersKeywordAndRequiresOnClause() {
		String sql = SqlQuery.query()
				.select(employee)
				.fullOuterJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.transpile();

		assertTrue(sql.contains(" FULL OUTER JOIN "));
		assertTrue(sql.contains(" ON "));
	}

	@Test
	void fromSubqueryCreatesDerivedTableWithAlias() {
		Query salarySummary = SqlQuery.query()
				.select(Employee.C_ID)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Employee.C_ID);

		var salaryView = SqlQuery.toTable(salarySummary);

		Query outer = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.join(salaryView).on(Employee.C_ID, salaryView.get("ID"))
				.where(salaryView.get("AVG_pay")).supOrEqTo(60000);

		String sql = outer.transpile();

		assertTrue(sql.contains("(SELECT"));
		String derivedAlias = quoted(salaryView.tableName());
		assertTrue(sql.contains(derivedAlias + " ON"));
		assertNotNull(salaryView.get("AVG_pay"));
		assertTrue(sql.contains(qualified(salaryView.tableName(), salaryView.get("AVG_pay").getName())));
	}

	@Test
	void whereInSubqueryTranslatesToInClause() {
		Query highSalaryIds = SqlQuery.query()
				.select(Job.C_EMPLOYEE_ID)
				.from(job)
				.where(Job.C_SALARY).supOrEqTo(60000);

		String sql = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.where(Employee.C_ID).in(highSalaryIds)
				.transpile();

		assertTrue(sql.contains(" IN ("));
		assertTrue(sql.contains("SELECT " + qualified(tableRef(job), "EMPLOYEE_ID")));
	}

	@Test
	void whereScalarSubqueryComparison() {
		Query averageSalary = SqlQuery.query()
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(job);

		String sql = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.where(Employee.C_ID).eq(averageSalary)
				.transpile();

		assertTrue(sql.contains(" = (SELECT AVG"));
	}

	@Test
	void whereExistsSubqueryAppendsExistsClause() {
		Query anyJob = SqlQuery.query()
				.select(Job.C_ID)
				.from(job);

		String sql = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.exists(anyJob)
				.transpile();

		assertTrue(sql.contains(" WHERE EXISTS ("));
	}

	@Test
	void scalarSubqueryRequiresSingleColumn() {
		Query invalid = SqlQuery.query()
				.select(Employee.C_ID)
				.select(Employee.C_FIRST_NAME)
				.from(employee);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> SqlQuery.query().select(Employee.C_FIRST_NAME)
						.from(employee)
						.where(Employee.C_ID).eq(invalid));
		assertTrue(ex.getMessage().contains("expected 1 column"));
	}

	@Test
	void existsSubqueryMustProjectColumns() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> SqlQuery.query().select(Employee.C_FIRST_NAME)
						.from(employee)
						.exists(SqlQuery.query()));
		assertTrue(ex.getMessage().contains("subquery selects no columns"));
	}

	@Test
	void whereOperatorsRequireExistingCondition() {
		Query query = SqlQuery.query();
		assertThrows(IllegalStateException.class, () -> query.eq("value"));
	}

	@Test
	void whereSupportsDescriptorShortcut() {
		Query query = SqlQuery.query();
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

		Query query = SqlQuery.query()
				.select(employee)
				.from(employee)
				.where(nameGroup);

		query.and(Employee.C_ID).eq(42);

		SqlAndParams rendered = query.render();
		String sql = rendered.sql();

		assertTrue(sql.contains("(" + qualified(tableRef(employee), "FIRST_NAME") + " = ? OR ("
				+ qualified(tableRef(employee), "LAST_NAME") + " = ?))"), () -> sql);
		assertTrue(sql.contains("AND " + qualified(tableRef(employee), "ID") + " = ?"), () -> sql);
		assertEquals(List.of("Alice", "Smith", 42), rendered.params());
	}

	@Test
	void likeOperatorFormatsStringLiteral() {
		SqlAndParams rendered = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.where(Employee.C_FIRST_NAME).like("%ice%")
				.render();

		assertTrue(rendered.sql().contains(" LIKE ? ESCAPE '\\'"), () -> rendered.sql());
		assertEquals(List.of("\\%ice\\%"), rendered.params());
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

		Query query = SqlQuery.query()
				.select(employee)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID);

		query.where(stateGroup);
		query.and(salaryGroup);

		SqlAndParams rendered = query.render();
		String sql = rendered.sql();

		assertTrue(sql.contains("(" + qualified(tableRef(employee), "LAST_NAME") + " = ? OR "
				+ qualified(tableRef(employee), "LAST_NAME") + " = ?)"), () -> sql);
		assertTrue(sql.contains("AND (" + qualified(tableRef(job), "SALARY") + " >= ? OR ("
				+ qualified(tableRef(job), "SALARY") + " BETWEEN ? AND ?))"), () -> sql);
		assertEquals(List.of("Miller", "Moore", 120_000, 80_000, 90_000), rendered.params());
	}

	@Test
	void groupedConditionsRespectHavingContext() {
		var highAverage = QueryHelper.group()
				.where(Job.C_SALARY).avg(Job.C_SALARY).supTo(60_000);

		var fallbackAverage = QueryHelper.group()
				.where(Job.C_SALARY).avg(Job.C_SALARY).supOrEqTo(55_000);

		Query query = SqlQuery.query()
				.select(Employee.C_ID)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Employee.C_ID);

		query.having(highAverage);
		query.and(fallbackAverage);

		SqlAndParams rendered = query.render();
		String sql = rendered.sql();

		String avgExpr = "AVG(" + qualified(tableRef(job), "SALARY") + ")";
		assertTrue(sql.contains("HAVING (" + avgExpr + " > ?) AND (" + avgExpr + " >= ?)"), () -> sql);
		assertFalse(sql.contains("WHERE AVG("));
		assertEquals(List.of(60_000, 55_000), rendered.params());
	}

	@Test
	void customerPaymentAggregations() {
		Query query = SqlQuery.query()
				.select(customerColumns.ID())
				.select(customerColumns.FIRST_NAME())
				.select(AggregateOperator.SUM, paymentColumns.AMOUNT())
				.from(customer)
				.join(orderHeader).on(customerColumns.ID(), orderHeaderColumns.CUSTOMER_ID())
				.join(payment).on(paymentColumns.ORDER_ID(), orderHeaderColumns.ID())
				.groupBy(customerColumns.ID(), customerColumns.FIRST_NAME())
				.orderBy(customerColumns.FIRST_NAME());

		String sql = query.transpile();

		assertTrue(sql.contains("FROM " + quoted(customer.getName())));
		assertTrue(sql.contains("JOIN " + quoted(orderHeader.getName())));
		assertTrue(sql.contains("JOIN " + quoted(payment.getName())));
		assertTrue(sql.contains("SUM(" + qualified(tableRef(payment), "AMOUNT") + ")"));
	}

	@Test
	void orderLineJoinProductProducesJoinClause() {
		Query query = SqlQuery.query()
				.select(productColumns.NAME())
				.select(orderLineColumns.QUANTITY())
				.from(orderLine)
				.join(product).on(orderLineColumns.PRODUCT_ID(), productColumns.ID());

		String sql = query.transpile();

		assertTrue(sql.contains("JOIN " + quoted(product.getName())));
		assertTrue(sql.contains(qualified(tableRef(orderLine), "PRODUCT_ID") + " = "
				+ qualified(tableRef(product), "ID")));
	}

	@Test
	void customersWithAverageOrderValueAboveThreshold() {
		Query query = SqlQuery.query()
				.select(customerColumns.ID())
				.select(customerColumns.FIRST_NAME())
				.from(customer)
				.join(orderHeader).on(customerColumns.ID(), orderHeaderColumns.CUSTOMER_ID())
				.groupBy(customerColumns.ID(), customerColumns.FIRST_NAME())
				.having(orderHeaderColumns.TOTAL()).avg(orderHeaderColumns.TOTAL())
				.supOrEqTo(new java.math.BigDecimal("500.00"));

		SqlAndParams rendered = query.render();
		assertTrue(rendered.sql().contains("AVG(" + qualified(tableRef(orderHeader), "TOTAL") + ") >= ?"));
		assertEquals(List.of(500), rendered.params());
	}

	@Test
	void betweenOperatorProducesRangeClause() {
		SqlAndParams rendered = SqlQuery.query()
				.select(Employee.C_ID)
				.where(Employee.C_ID).between(1, 10)
				.render();

		assertTrue(rendered.sql().contains(" BETWEEN ? AND ?"));
		assertEquals(List.of(1, 10), rendered.params());
	}

	@Test
	void isNullOperatorOmitsParentheses() {
		String sql = SqlQuery.query()
				.select(Employee.C_LAST_NAME)
				.where(Employee.C_LAST_NAME).isNull()
				.transpile();

		assertTrue(sql.contains(" IS NULL"));
		assertFalse(sql.contains(" IS NULL()"));
	}

	@Test
	void notInOperatorRendersNotInClause() {
		SqlAndParams rendered = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.where(Employee.C_FIRST_NAME).notIn("Alice", "Bob")
				.render();

		assertTrue(rendered.sql().contains(" NOT IN (?, ?)"));
		assertEquals(List.of("Alice", "Bob"), rendered.params());
	}


	@Test
	void groupByClauseAppearsAfterWhere() {
		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME).groupBy(Employee.C_FIRST_NAME);

		String sql = query.transpile();

		assertTrue(sql.contains(" GROUP BY "));
		assertTrue(sql.contains(Employee.C_FIRST_NAME.column().transpile(false)));
	}

	@Test
	void orderBySupportsAscendingAndDescending() {
		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME).orderBy(Employee.C_LAST_NAME)
				.orderBy(Employee.C_ID, SortDirection.DESC);

		String sql = query.transpile();

		assertTrue(sql.contains(" ORDER BY "));
		assertTrue(sql.contains(Employee.C_LAST_NAME.column().transpile(false) + " ASC"));
		assertTrue(sql.contains(Employee.C_ID.column().transpile(false) + " DESC"));
	}

	@Test
	void havingClauseFollowsGroupBy() {
		Query query = SqlQuery.query();
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
		Query query = SqlQuery.query();
		query.select(Job.C_EMPLOYEE_ID).select(AggregateOperator.AVG, Job.C_SALARY)
				.groupBy(Job.C_EMPLOYEE_ID)
				.having(Job.C_SALARY).avg(Job.C_SALARY).supTo(50000);

		SqlAndParams rendered = query.render();
		String sql = rendered.sql();

		assertTrue(sql.contains(" HAVING AVG("));
		assertTrue(sql.contains(Job.C_SALARY.column().transpile(false)));
		assertTrue(sql.contains(" > ?"));
		assertEquals(List.of(50_000), rendered.params());
	}

	@Test
	void havingBetweenSupportsNumericRange() {
		Query query = SqlQuery.query();
		query.select(Job.C_EMPLOYEE_ID)
				.select(AggregateOperator.SUM, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Job.C_EMPLOYEE_ID)
				.having(Job.C_SALARY).between(50000, 100000);

		SqlAndParams rendered = query.render();
		String sql = rendered.sql();

		assertTrue(sql.contains(" HAVING "));
		assertTrue(sql.contains(" BETWEEN ? AND ?"));
		assertEquals(List.of(50_000, 100_000), rendered.params());
	}

	@Test
	void limitAndOffsetRenderWithOracleSyntax() {
		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME).orderBy(Employee.C_FIRST_NAME)
				.limitAndOffset(10, 5);

		SqlAndParams rendered = query.render();
		String sql = rendered.sql();

		assertTrue(sql.contains(" OFFSET ? ROWS"));
		assertTrue(sql.contains(" FETCH NEXT ? ROWS ONLY"));
		assertEquals(List.of(5L, 10L), rendered.params());
	}

	@Test
	void selectTranspilerKeepsAggregateFormatting() {
		String sql = SqlQuery.query().select(AggregateOperator.MAX, Employee.C_FIRST_NAME)
				.select(Employee.C_LAST_NAME).transpile();

		assertTrue(sql.startsWith("SELECT MAX("));
		assertTrue(sql.contains("), "));
		assertTrue(sql.contains(Employee.C_LAST_NAME.column().transpile(false)));
	}

	@Test
	void countAllProducesCountStar() {
		String sql = SqlQuery.countAll().transpile();
		assertTrue(sql.startsWith("SELECT COUNT(*)"));
	}

	@Test
	void distinctSelectsRenderDistinctKeyword() {
		String sql = SqlQuery.query()
				.distinct()
				.select(Employee.C_FIRST_NAME)
				.transpile();

		assertTrue(sql.startsWith("SELECT DISTINCT "));
		assertTrue(sql.contains(Employee.C_FIRST_NAME.column().transpile(false)));
	}

	@Test
	void countColumnRegistersTable() {
		Query query = SqlQuery.query().count(Employee.C_ID);
		String sql = query.transpile();
		assertTrue(sql.contains("COUNT(" + Employee.C_ID.column().transpile(false) + ")"));
		assertTrue(sql.contains(" FROM "));
	}

	@Test
	void prettyPrintBreaksClausesAcrossLines() {
		Query query = SqlQuery.query();
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
		Query left = SqlQuery.query().select(employee);
		Query right = SqlQuery.query().select(job);

		String sql = left.union(right).transpile();

		String expected = "SELECT " + qualified(tableRef(employee), "ID") + ", "
				+ qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName") + ", "
				+ qualified(tableRef(employee), "LAST_NAME") + " as " + quoted("lastName") + ", "
				+ qualified(tableRef(employee), "MAIL") + " as " + quoted("email") + ", "
				+ qualified(tableRef(employee), "PASSWORD") + " as " + quoted("passwd") + " FROM "
				+ quoted(employee.getName()) + " " + quoted(tableRef(employee)) + " UNION (SELECT "
				+ qualified(tableRef(job), "ID") + ", " + qualified(tableRef(job), "SALARY") + " as "
				+ quoted("pay") + ", " + qualified(tableRef(job), "DESCRIPTION") + " as " + quoted("Intitule")
				+ ", " + qualified(tableRef(job), "EMPLOYEE_ID") + " as " + quoted("employeeId")
				+ " FROM " + quoted(job.getName()) + " " + quoted(tableRef(job)) + ")";
		assertEquals(expected, sql);
	}

	@Test
	void unionAllKeepsDuplicates() {
		Query base = SqlQuery.query().select(Employee.C_FIRST_NAME);
		Query other = SqlQuery.query().select(Employee.C_FIRST_NAME);

		String sql = base.unionAll(other).transpile();

		assertTrue(sql.contains("UNION ALL"));
	}

	@Test
	void intersectProducesIntersection() {
		Query left = SqlQuery.query().select(Employee.C_ID);
		Query right = SqlQuery.query().select(Job.C_EMPLOYEE_ID);

		String sql = left.intersect(right).transpile();
		assertTrue(sql.contains("INTERSECT"));
	}

	@Test
	void exceptUsesMinusForOracle() {
		Query left = SqlQuery.query().select(employee);
		Query right = SqlQuery.query().select(job);

		String sql = left.except(right).transpile();
		assertTrue(sql.contains("MINUS"));
	}

	@Test
	void exceptAllThrowsUnsupported() {
		Query left = SqlQuery.query().select(employee);
		Query right = SqlQuery.query().select(job);

		assertThrows(UnsupportedOperationException.class, () -> left.exceptAll(right).transpile());
	}

	private static final class DialectsTestDialect implements Dialect {

		@Override
		public String id() {
			return "test-brackets";
		}

		@Override
		public String quoteIdent(String raw) {
			return "[" + raw + "]";
		}

		@Override
		public char likeEscapeChar() {
			return '#';
		}

		@Override
		public String exceptOperator(boolean all) {
			return all ? "EXCEPT ALL" : "EXCEPT";
		}

		@Override
		public PaginationClause renderLimitOffset(Long limit, Long offset) {
			StringBuilder sql = new StringBuilder();
			java.util.List<Object> params = new java.util.ArrayList<>();
			if (limit != null) {
				sql.append(" LIMIT ?");
				params.add(limit.longValue());
			}
			if (offset != null) {
				sql.append(" OFFSET ?");
				params.add(offset.longValue());
			}
			return new PaginationClause(sql.toString(), params);
		}

		@Override
		public String renderFunction(String logicalName, java.util.List<String> argsSql) {
			return logicalName.toUpperCase(java.util.Locale.ROOT) + '(' + String.join(", ", argsSql) + ')';
		}
	}

	@Test
	void inlineFormatterProducesLiteralSql() {
		SqlAndParams sp = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.where(Employee.C_FIRST_NAME).eq("Alice")
				.render();

		String literal = SqlFormatter.inlineLiterals(sp, Dialects.defaultDialect());
		assertTrue(literal.contains("'Alice'"));
	}

}
