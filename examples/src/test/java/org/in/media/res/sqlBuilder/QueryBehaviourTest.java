package org.in.media.res.sqlBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.CteRef;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.Dialects;
import org.in.media.res.sqlBuilder.api.query.QueryColumns;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlFormatter;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.WithBuilder;
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
import org.in.media.res.sqlBuilder.api.query.SetOperator;
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
				.render().sql();

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

		String expected = "SELECT " + qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName")
				+ " FROM " + quoted(employee.getName()) + " " + quoted(tableRef(employee)) + " LIMIT ? OFFSET ?";
		assertEquals(expected, rendered.sql());
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
				.render().sql();

		assertTrue(sql.contains(quoted(tableRef(helper.table()))));
		assertTrue(sql.contains("LIKE"));
	}

	@Test
	void fromVarargsIncludesAllTables() {
		Query query = SqlQuery.query();
		query.from(employee, job);

		String sql = query.render().sql();

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
		String sql = query.render().sql();
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
	void optionalConditionsGuardPredicatesAndBindTwice() {
		SqlParameter<String> firstName = SqlParameters.param("firstName");
		CompiledQuery compiled = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.whereOptionalEquals(Employee.C_FIRST_NAME, firstName)
				.asQuery()
				.compile();

		String expected = "SELECT " + qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName")
				+ " FROM " + quoted(employee.getName()) + " " + quoted(tableRef(employee))
				+ " WHERE (? IS NULL OR " + qualified(tableRef(employee), "FIRST_NAME") + " = ?)";
		assertEquals(expected, compiled.sql());

		SqlAndParams params = compiled.bind(Map.of("firstName", "Ada"));
		assertEquals(List.of("Ada", "Ada"), params.params());
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

		String sql = select.render().sql();

		assertTrue(sql.contains(employee.get(Employee.C_FIRST_NAME).transpile(false)));
		assertTrue(sql.contains(employee.get(Employee.C_LAST_NAME).transpile(false)));
	}

	@Test
	void querySelectRegistersBaseTableAutomatically() {
		Query query = SqlQuery.query();
		String sql = query.select(employee).render().sql();

		assertTrue(sql.contains(" FROM "));
		assertTrue(sql.contains(employee.getName()));
	}

	@Test
	void querySelectColumnRegistersBaseTableAutomatically() {
		Query query = SqlQuery.query();
		String sql = query.select(Employee.C_FIRST_NAME).render().sql();

		assertTrue(sql.contains(" FROM "));
		assertTrue(sql.contains(employee.getName()));
	}

	@Test
	void joinSupportsDescriptorShortcut() {
		Query query = SqlQuery.query();
		String sql = query.select(Employee.C_FIRST_NAME).innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID).render().sql();

		assertTrue(sql.contains(" JOIN "));
		assertTrue(sql.contains(Employee.C_ID.column().transpile(false)));
		assertTrue(sql.contains(Job.C_EMPLOYEE_ID.column().transpile(false)));
	}

	@Test
	void crossJoinProducesCrossJoinKeyword() {
		String sql = SqlQuery.query()
				.select(employee)
				.crossJoin(job)
				.render().sql();

		assertTrue(sql.contains(" CROSS JOIN "));
		assertFalse(sql.contains(" ON "));
	}

	@Test
	void fullOuterJoinRendersKeywordAndRequiresOnClause() {
		String sql = SqlQuery.query()
				.select(employee)
				.fullOuterJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.render().sql();

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

		var salaryView = SqlQuery.toTable(salarySummary, "SALARY_VIEW", "EMPLOYEE_ID", "AVG_SALARY");

		Query outer = SqlQuery.query()
				.select(Employee.C_FIRST_NAME)
				.from(employee)
				.join(salaryView).on(Employee.C_ID, salaryView.get("EMPLOYEE_ID"))
				.where(salaryView.get("AVG_SALARY")).supOrEqTo(60000);

		String sql = outer.render().sql();

		String inner = "SELECT " + qualified(tableRef(employee), "ID") + ", AVG("
				+ qualified(tableRef(job), "SALARY") + ") FROM " + quoted(employee.getName()) + " "
				+ quoted(tableRef(employee)) + " JOIN " + quoted(job.getName()) + " " + quoted(tableRef(job)) + " ON "
				+ qualified(tableRef(employee), "ID") + " = " + qualified(tableRef(job), "EMPLOYEE_ID") + " GROUP BY "
				+ qualified(tableRef(employee), "ID");
		String expected = "SELECT " + qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName")
				+ " FROM " + quoted(employee.getName()) + " " + quoted(tableRef(employee)) + " JOIN (" + inner + ") "
				+ quoted("SALARY_VIEW") + " ON " + qualified(tableRef(employee), "ID") + " = "
				+ quoted("SALARY_VIEW") + "." + quoted("EMPLOYEE_ID") + " WHERE " + quoted("SALARY_VIEW") + "."
				+ quoted("AVG_SALARY") + " >= ?";
		assertEquals(expected, sql);
	}

	@Test
	void chainedCteBuilderRendersWithClause() {
		Query jobEmployeeIds = SqlQuery.query()
				.select(Job.C_EMPLOYEE_ID)
				.from(job)
				.where(Job.C_SALARY).supOrEqTo(120_000)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		WithBuilder.CteStep step = with.with("high_salary_ids", jobEmployeeIds, "EMPLOYEE_ID");
		CteRef highSalaryIds = step.ref();

		Query main = step.and().main(SqlQuery.query()
				.select(highSalaryIds.column("EMPLOYEE_ID"))
				.from(highSalaryIds)
				.asQuery());

		String sql = main.render().sql();

		String inner = "SELECT " + qualified(tableRef(job), "EMPLOYEE_ID") + " FROM " + quoted(job.getName()) + " "
				+ quoted(tableRef(job)) + " WHERE " + qualified(tableRef(job), "SALARY") + " >= ?";
		// Job.C_EMPLOYEE_ID carries alias employeeId; CTE preserves it in raw SQL.
		String expected = "WITH \"high_salary_ids\"(\"EMPLOYEE_ID\") AS (SELECT " + qualified(tableRef(job), "EMPLOYEE_ID")
				+ " as " + quoted("employeeId") + " FROM " + quoted(job.getName()) + " " + quoted(tableRef(job)) + " WHERE "
				+ qualified(tableRef(job), "SALARY") + " >= ?) SELECT " + quoted("high_salary_ids") + "."
				+ quoted("EMPLOYEE_ID") + " FROM " + quoted("high_salary_ids");

		assertEquals(expected, sql);
	}

	@Test
	void stagedCteBuilderUsesAsSyntax() {
		Query avgSalary = SqlQuery.query()
				.select(Employee.C_ID)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Employee.C_ID)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		WithBuilder.CteStep step = with.with("salary_avg").as(avgSalary, "EMPLOYEE_ID", "AVG_SALARY");
		CteRef salaryAvg = step.ref();

		Query main = step.and().main(SqlQuery.query()
				.select(salaryAvg.column("EMPLOYEE_ID"))
				.from(salaryAvg)
				.asQuery());

		String sql = main.render().sql();
		assertTrue(sql.startsWith("WITH \"salary_avg\"(\"EMPLOYEE_ID\", \"AVG_SALARY\") AS"));
	}

	@Test
	void multipleNestedCtesRenderExpectedSql() {
		Query salaryDetailsQuery = SqlQuery.query()
				.select(Employee.C_ID)
				.select(Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.asQuery();

		WithBuilder with = SqlQuery.with();
		CteRef salaryDetails = with.cte("salary_details", salaryDetailsQuery, "employee_id", "salary");

		Query avgSalaryQuery = SqlQuery.query()
				.select(salaryDetails.column("employee_id"))
				.select(AggregateOperator.AVG, salaryDetails.column("salary"))
				.from(salaryDetails)
				.groupBy(salaryDetails.column("employee_id"))
				.asQuery();

		CteRef avgSalary = with.cte("avg_salary", avgSalaryQuery, "employee_id", "avg_salary");

		Query main = with.main(SqlQuery.query()
				.select(avgSalary.column("employee_id"))
				.from(avgSalary)
				.where(avgSalary.column("avg_salary")).supOrEqTo(90000)
				.asQuery());

		String sql = main.render().sql();

		String salaryDetailsSql = "SELECT " + qualified(tableRef(employee), "ID") + ", "
				+ qualified(tableRef(job), "SALARY") + " as " + quoted("pay") + " FROM " + quoted(employee.getName()) + " "
				+ quoted(tableRef(employee)) + " JOIN " + quoted(job.getName()) + " " + quoted(tableRef(job)) + " ON "
				+ qualified(tableRef(employee), "ID") + " = " + qualified(tableRef(job), "EMPLOYEE_ID");
		String avgSalarySql = "SELECT " + quoted("salary_details") + "." + quoted("employee_id") + ", AVG("
				+ quoted("salary_details") + "." + quoted("salary") + ") FROM " + quoted("salary_details")
				+ " GROUP BY " + quoted("salary_details") + "." + quoted("employee_id");
		String expected = "WITH \"salary_details\"(\"employee_id\", \"salary\") AS (" + salaryDetailsSql
				+ "), \"avg_salary\"(\"employee_id\", \"avg_salary\") AS (" + avgSalarySql + ") SELECT "
				+ quoted("avg_salary") + "." + quoted("employee_id") + " FROM " + quoted("avg_salary") + " WHERE "
				+ quoted("avg_salary") + "." + quoted("avg_salary") + " >= ?";
		assertEquals(expected, sql);
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
				.render().sql();

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
				.render().sql();

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
				.render().sql();

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

		String sql = query.render().sql();

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

		String sql = query.render().sql();

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

		String sql = query.render().sql();

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
				.render().sql();

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

		String sql = query.render().sql();

		assertTrue(sql.contains(" GROUP BY "));
		assertTrue(sql.contains(Employee.C_FIRST_NAME.column().transpile(false)));
	}

	@Test
	void orderBySupportsAscendingAndDescending() {
		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME).orderBy(Employee.C_LAST_NAME)
				.orderBy(Employee.C_ID, SortDirection.DESC);

		String sql = query.render().sql();

		assertTrue(sql.contains(" ORDER BY "));
		assertTrue(sql.contains(Employee.C_LAST_NAME.column().transpile(false) + " ASC"));
		assertTrue(sql.contains(Employee.C_ID.column().transpile(false) + " DESC"));
	}

	@Test
	void havingClauseFollowsGroupBy() {
		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME).groupBy(Employee.C_FIRST_NAME)
				.having(Employee.C_FIRST_NAME).eq("Alice");

		String sql = query.render().sql();

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
		String expected = "SELECT " + qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName")
				+ " FROM " + quoted(employee.getName()) + " " + quoted(tableRef(employee)) + " ORDER BY "
				+ qualified(tableRef(employee), "FIRST_NAME") + " ASC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		assertEquals(expected, rendered.sql());
		assertEquals(List.of(5L, 10L), rendered.params());
	}

	@Test
	void selectTranspilerKeepsAggregateFormatting() {
		String sql = SqlQuery.query().select(AggregateOperator.MAX, Employee.C_FIRST_NAME)
				.select(Employee.C_LAST_NAME).render().sql();

		assertTrue(sql.startsWith("SELECT MAX("));
		assertTrue(sql.contains("), "));
		assertTrue(sql.contains(Employee.C_LAST_NAME.column().transpile(false)));
	}

	@Test
	void countAllProducesCountStar() {
		String sql = SqlQuery.countAll().asQuery().render().sql();
		assertTrue(sql.startsWith("SELECT COUNT(*)"));
	}

	@Test
	void distinctSelectsRenderDistinctKeyword() {
		String sql = SqlQuery.query()
				.distinct()
				.select(Employee.C_FIRST_NAME)
				.render().sql();

		assertTrue(sql.startsWith("SELECT DISTINCT "));
		assertTrue(sql.contains(Employee.C_FIRST_NAME.column().transpile(false)));
	}

	@Test
	void countColumnRegistersTable() {
		Query query = SqlQuery.query().count(Employee.C_ID);
		String sql = query.render().sql();
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

		String pretty = query.prettyPrint().strip();
		String expected = """
SELECT
  %s
FROM
  %s
WHERE
  %s = ?
ORDER BY
  %s ASC
				"""
				.formatted(
						qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName"),
						quoted(employee.getName()) + " " + quoted(tableRef(employee)),
						qualified(tableRef(employee), "FIRST_NAME"),
						qualified(tableRef(employee), "FIRST_NAME"));
		assertEquals(expected.strip(), pretty);
	}

	@Test
	void prettyPrintOmitsParenthesesForImplicitSingletonGroup() {
		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME);
		query.from(employee);

		var implicitGroup = new org.in.media.res.sqlBuilder.core.query.ConditionGroupBuilder()
				.where(Employee.C_FIRST_NAME)
				.eq("Alice")
				.build();

		query.where(implicitGroup);

		String pretty = query.prettyPrint().strip();
		System.out.println("prettyPrintOmitsParenthesesForImplicitSingletonGroup:\n" + pretty + "\n");
		String expected = """
SELECT
  %s
FROM
  %s
WHERE
  %s = ?
				"""
				.formatted(
						qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName"),
						quoted(employee.getName()) + " " + quoted(tableRef(employee)),
						qualified(tableRef(employee), "FIRST_NAME"));

		assertEquals(expected.strip(), pretty);
	}

	@Test
	void unionCombinesQueries() {
		Query left = SqlQuery.query().select(employee);
		Query right = SqlQuery.query().select(job);

		String sql = left.union(right).render().sql();

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

		String sql = base.unionAll(other).render().sql();

		String expected = "SELECT " + qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName")
				+ " FROM " + quoted(employee.getName()) + " " + quoted(tableRef(employee)) + " UNION ALL (SELECT "
				+ qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName") + " FROM "
				+ quoted(employee.getName()) + " " + quoted(tableRef(employee)) + ")";
		assertEquals(expected, sql);
	}

	@Test
	void intersectProducesIntersection() {
		Query left = SqlQuery.query().select(Employee.C_ID).from(employee);
		Query right = SqlQuery.query().select(Job.C_EMPLOYEE_ID).from(job);

		String sql = left.intersect(right).render().sql();
		String expected = "SELECT " + qualified(tableRef(employee), "ID") + " FROM " + quoted(employee.getName()) + " "
				+ quoted(tableRef(employee)) + " INTERSECT (SELECT " + qualified(tableRef(job), "EMPLOYEE_ID") + " as "
				+ quoted("employeeId") + " FROM " + quoted(job.getName()) + " " + quoted(tableRef(job)) + ")";
		assertEquals(expected, sql);
	}

	@Test
	void exceptUsesMinusForOracle() {
		Query left = SqlQuery.query().select(employee).from(employee);
		Query right = SqlQuery.query().select(job).from(job);

		String sql = left.except(right).render().sql();
		String expected = "SELECT " + qualified(tableRef(employee), "ID") + ", "
				+ qualified(tableRef(employee), "FIRST_NAME") + " as " + quoted("firstName") + ", "
				+ qualified(tableRef(employee), "LAST_NAME") + " as " + quoted("lastName") + ", "
				+ qualified(tableRef(employee), "MAIL") + " as " + quoted("email") + ", "
				+ qualified(tableRef(employee), "PASSWORD") + " as " + quoted("passwd") + " FROM "
				+ quoted(employee.getName()) + " " + quoted(tableRef(employee)) + " MINUS (SELECT "
				+ qualified(tableRef(job), "ID") + ", " + qualified(tableRef(job), "SALARY") + " as " + quoted("pay")
				+ ", " + qualified(tableRef(job), "DESCRIPTION") + " as " + quoted("Intitule") + ", "
				+ qualified(tableRef(job), "EMPLOYEE_ID") + " as " + quoted("employeeId") + " FROM "
				+ quoted(job.getName()) + " " + quoted(tableRef(job)) + ")";
		assertEquals(expected, sql);
	}

	@Test
	void intersectAllProducesExpectedSql() {
		Query left = SqlQuery.query().select(Employee.C_ID).from(employee);
		Query right = SqlQuery.query().select(Job.C_EMPLOYEE_ID).from(job);

		String sql = left.intersectAll(right).render().sql();
		String expected = "SELECT " + qualified(tableRef(employee), "ID") + " FROM " + quoted(employee.getName()) + " "
				+ quoted(tableRef(employee)) + " INTERSECT ALL (SELECT " + qualified(tableRef(job), "EMPLOYEE_ID")
				+ " as " + quoted("employeeId") + " FROM " + quoted(job.getName()) + " " + quoted(tableRef(job)) + ")";
		assertEquals(expected, sql);
	}

	@Test
	void postgresExceptAllRendersDialectSpecificKeyword() {
		Query left = SqlQuery.newQuery(Dialects.postgres()).asQuery()
				.select(Employee.C_ID)
				.from(employee);
		Query right = SqlQuery.newQuery(Dialects.postgres()).asQuery()
				.select(Job.C_EMPLOYEE_ID)
				.from(job);

		String sql = left.exceptAll(right).render().sql();
		String expected = "SELECT " + qualified(tableRef(employee), "ID") + " FROM " + quoted(employee.getName()) + " "
				+ quoted(tableRef(employee)) + " EXCEPT ALL (SELECT " + qualified(tableRef(job), "EMPLOYEE_ID")
				+ " as " + quoted("employeeId") + " FROM " + quoted(job.getName()) + " " + quoted(tableRef(job)) + ")";
		assertEquals(expected, sql);
	}

	@Test
	void exceptAllThrowsUnsupported() {
		Query left = SqlQuery.query().select(employee);
		Query right = SqlQuery.query().select(job);

		assertThrows(UnsupportedOperationException.class, () -> left.exceptAll(right).render().sql());
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
		public String setOperator(SetOperator operator) {
			return operator.sql();
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
