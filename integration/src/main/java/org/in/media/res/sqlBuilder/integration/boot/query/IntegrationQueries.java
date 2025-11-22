package org.in.media.res.sqlBuilder.integration.boot.query;

import java.util.HashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.QueryHelper;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.integration.model.CustomersTable;
import org.in.media.res.sqlBuilder.integration.model.DepartmentsTable;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.JobsTable;
import org.in.media.res.sqlBuilder.integration.model.OrderLinesTable;
import org.in.media.res.sqlBuilder.integration.model.OrdersTable;
import org.in.media.res.sqlBuilder.integration.model.PaymentsTable;
import org.in.media.res.sqlBuilder.integration.model.ProductsTable;

public final class IntegrationQueries {

	private IntegrationQueries() {
	}

	public static SqlAndParams simpleProjection() {
		Table employees = IntegrationSchema.employees();
		Query query = SqlQuery.query();
		query.select(employees)
				.from(employees)
				.orderBy(EmployeesTable.C_ID)
				.limit(10);
		return query.render();
	}

	public static SqlAndParams joinsWithFilters() {
		Table employees = IntegrationSchema.employees();
		Table jobs = IntegrationSchema.jobs();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME, JobsTable.C_TITLE)
				.from(employees)
				.leftJoin(jobs).on(EmployeesTable.C_ID, JobsTable.C_EMPLOYEE_ID)
				.where(JobsTable.C_SALARY).supOrEqTo(50_000)
				.orderBy(EmployeesTable.C_FIRST_NAME);
		return query.render();
	}

	public static SqlAndParams aggregations() {
		Table employees = IntegrationSchema.employees();
		Table jobs = IntegrationSchema.jobs();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME)
				.select(AggregateOperator.AVG, JobsTable.C_SALARY)
				.from(employees)
				.join(jobs).on(EmployeesTable.C_ID, JobsTable.C_EMPLOYEE_ID)
				.groupBy(EmployeesTable.C_FIRST_NAME)
				.having(JobsTable.C_SALARY).avg(JobsTable.C_SALARY).supTo(60_000)
				.orderBy(EmployeesTable.C_FIRST_NAME);

		return query.render();
	}

	public static SqlAndParams pagination() {
		Table jobs = IntegrationSchema.jobs();
		Query query = SqlQuery.query();
		query.select(JobsTable.C_TITLE)
				.from(jobs)
				.orderBy(JobsTable.C_SALARY, SortDirection.DESC)
				.limitAndOffset(10, 1);

		return query.render();
	}

	public static SqlAndParams countEmployees() {
		Table employees = IntegrationSchema.employees();

		Query countQuery = SqlQuery.countAll()
				.from(employees)
				.asQuery();
		return countQuery.render();
	}

	public static SqlAndParams activeEmployees() {
		Table employees = IntegrationSchema.employees();

		Query printable = SqlQuery.newQuery()
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.orderBy(EmployeesTable.C_LAST_NAME)
				.asQuery();
		printable.where(EmployeesTable.C_STATUS).eq("ACTIVE");

		return printable.render();
	}

	public static SqlAndParams optimizerHints() {
		Table employees = IntegrationSchema.employees();
		Query query = SqlQuery.newQuery()
				.hint("/*+ INDEX(e employees_department_id_idx) */")
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.asQuery();
		query.where(EmployeesTable.C_STATUS).eq("ACTIVE")
				.orderBy(EmployeesTable.C_LAST_NAME);

		return query.render();
	}

	public static SqlAndParams setOperations() {
		Table employees = IntegrationSchema.employees();
		Table customers = IntegrationSchema.customers();

		Query customerNames = SqlQuery.newQuery()
				.select(CustomersTable.C_FIRST_NAME)
				.select(CustomersTable.C_LAST_NAME)
				.from(customers)
				.asQuery();

		Query union = SqlQuery.newQuery()
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.asQuery()
				.union(customerNames);

		return union.render();
	}

	public static SqlAndParams derivedTables() {
		Table employees = IntegrationSchema.employees();
		Table jobs = IntegrationSchema.jobs();

		Query salarySummary = SqlQuery.newQuery()
				.select(EmployeesTable.C_ID)
				.select(AggregateOperator.AVG, JobsTable.C_SALARY)
				.from(employees)
				.join(jobs).on(EmployeesTable.C_ID, JobsTable.C_EMPLOYEE_ID)
				.groupBy(EmployeesTable.C_ID)
				.asQuery();

		Table salaryAvg = SqlQuery.toTable(salarySummary, "salary_avg", "EMPLOYEE_ID", "AVG_SALARY");

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.join(salaryAvg).on(EmployeesTable.C_ID, salaryAvg.get("EMPLOYEE_ID"))
				.where(salaryAvg.get("AVG_SALARY")).supOrEqTo(80_000)
				.orderBy(EmployeesTable.C_LAST_NAME);

		return query.render();
	}

	public static SqlAndParams cte() {
		Table employees = IntegrationSchema.employees();
		Table jobs = IntegrationSchema.jobs();

		Query avgSalary = SqlQuery.newQuery()
				.select(EmployeesTable.C_ID)
				.select(AggregateOperator.AVG, JobsTable.C_SALARY)
				.from(employees)
				.join(jobs).on(EmployeesTable.C_ID, JobsTable.C_EMPLOYEE_ID)
				.groupBy(EmployeesTable.C_ID)
				.asQuery();

		var with = SqlQuery.with();
		var salaryAvg = with.cte("salary_avg", avgSalary, "EMPLOYEE_ID", "AVG_SALARY");

		Query main = SqlQuery.newQuery()
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.join(salaryAvg).on(EmployeesTable.C_ID, salaryAvg.column("EMPLOYEE_ID"))
				.where(salaryAvg.column("AVG_SALARY")).supOrEqTo(85_000)
				.asQuery();

		return with.main(main).render();
	}

	public static SqlAndParams subqueryFiltering() {
		Table jobs = IntegrationSchema.jobs();
		Table employees = IntegrationSchema.employees();

		Query highSalaryIds = SqlQuery.newQuery()
				.select(JobsTable.C_EMPLOYEE_ID)
				.from(jobs)
				.where(JobsTable.C_SALARY).supOrEqTo(80_000)
				.asQuery();

		Query jobExists = SqlQuery.newQuery()
				.select(JobsTable.C_ID)
				.from(jobs)
				.where(JobsTable.C_JOB_TYPE).eq("FULL_TIME")
				.asQuery();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.where(EmployeesTable.C_ID).in(highSalaryIds)
				.exists(jobExists)
				.orderBy(EmployeesTable.C_LAST_NAME);

		return query.render();
	}

	public static SqlAndParams optionalFiltersDisabled() {
		return optionalFilters(null, null, null);
	}

	public static SqlAndParams optionalFiltersEnabled() {
		return optionalFilters("Alice", "Montreal", 90_000);
	}

	public static SqlAndParams groupedFilters() {
		Table employees = IntegrationSchema.employees();
		Table departments = IntegrationSchema.departments();

		var locationGroup = QueryHelper.group()
				.where(DepartmentsTable.C_LOCATION).eq("Montreal")
				.or(DepartmentsTable.C_LOCATION).eq("Quebec City");

		var salaryGroup = QueryHelper.group()
				.where(EmployeesTable.C_SALARY).supOrEqTo(120_000)
				.orGroup()
				.where(EmployeesTable.C_SALARY).between(80_000, 90_000)
				.endGroup();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.join(departments).on(EmployeesTable.C_DEPARTMENT_ID, DepartmentsTable.C_ID)
				.where(locationGroup)
				.and(salaryGroup)
				.orderBy(EmployeesTable.C_LAST_NAME);

		return query.render();
	}

	public static SqlAndParams rawSqlFragments() {
		Query query = SqlQuery.query();
		query.selectRaw("c.first_name || ' ' || c.last_name AS customer_name");
		query.selectRaw("SUM(o.total) AS order_total");
		query.fromRaw("customers c");
		query.joinRaw("orders o ON o.customer_id = c.id");
		query.whereRaw("o.status <> 'PENDING'");
		query.groupByRaw("c.first_name, c.last_name");
		query.havingRaw("SUM(o.total) > 300");
		query.orderByRaw("order_total DESC");

		return query.render();
	}

	public static SqlAndParams departmentSalaryTotals() {
		Table departments = IntegrationSchema.departments();
		Table employees = IntegrationSchema.employees();

		Query query = SqlQuery.query();
		query.select(DepartmentsTable.C_NAME)
				.select(AggregateOperator.SUM, EmployeesTable.C_SALARY)
				.select(AggregateOperator.SUM, EmployeesTable.C_BONUS)
				.from(departments)
				.leftJoin(employees).on(DepartmentsTable.C_ID, EmployeesTable.C_DEPARTMENT_ID)
				.groupBy(DepartmentsTable.C_NAME)
				.orderBy(DepartmentsTable.C_NAME);

		return query.render();
	}

	public static SqlAndParams topPaidEmployees() {
		Table employees = IntegrationSchema.employees();

		Query query = SqlQuery.query();
		query.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.select(EmployeesTable.C_SALARY)
				.from(employees)
				.where(EmployeesTable.C_STATUS).eq("ACTIVE")
				.orderBy(EmployeesTable.C_SALARY, SortDirection.DESC)
				.limit(3);

		return query.render();
	}

	public static SqlAndParams ordersWithCustomers() {
		Table orders = IntegrationSchema.orders();
		Table customers = IntegrationSchema.customers();

		Query query = SqlQuery.query();
		query.select(OrdersTable.C_ID)
				.select(CustomersTable.C_FIRST_NAME)
				.select(CustomersTable.C_LAST_NAME)
				.select(OrdersTable.C_TOTAL)
				.select(OrdersTable.C_STATUS)
				.from(orders)
				.join(customers).on(OrdersTable.C_CUSTOMER_ID, CustomersTable.C_ID)
				.where(OrdersTable.C_TOTAL).supOrEqTo(300)
				.orderBy(OrdersTable.C_TOTAL, SortDirection.DESC);

		return query.render();
	}

	public static SqlAndParams productRevenue() {
		Table orderLines = IntegrationSchema.orderLines();
		Table products = IntegrationSchema.products();
		Table orders = IntegrationSchema.orders();
		Table payments = IntegrationSchema.payments();

		Query query = SqlQuery.query();
		query.select(ProductsTable.C_NAME)
				.select(AggregateOperator.SUM, OrderLinesTable.C_QUANTITY)
				.selectRaw("SUM(ol.unit_price * ol.quantity) AS line_total")
				.select(AggregateOperator.SUM, PaymentsTable.C_AMOUNT)
				.from(orderLines)
				.join(products).on(OrderLinesTable.C_PRODUCT_ID, ProductsTable.C_ID)
				.join(orders).on(OrderLinesTable.C_ORDER_ID, OrdersTable.C_ID)
				.leftJoin(payments).on(PaymentsTable.C_ORDER_ID, OrdersTable.C_ID)
				.groupBy(ProductsTable.C_NAME)
				.orderByRaw("line_total DESC");

		return query.render();
	}

	private static SqlAndParams optionalFilters(String nameFilter, String cityFilter, Integer minSalaryFilter) {
		Table employees = IntegrationSchema.employees();
		Table departments = IntegrationSchema.departments();

		SqlParameter<String> name = SqlParameters.param("nameFilter");
		SqlParameter<String> city = SqlParameters.param("cityFilter");
		SqlParameter<Integer> minSalary = SqlParameters.param("minSalary");

		Query query = SqlQuery.newQuery()
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.select(DepartmentsTable.C_NAME)
				.from(employees)
				.join(departments).on(EmployeesTable.C_DEPARTMENT_ID, DepartmentsTable.C_ID)
				.whereOptionalEquals(EmployeesTable.C_FIRST_NAME, name)
				.whereOptionalEquals(DepartmentsTable.C_LOCATION, city)
				.whereOptionalGreaterOrEqual(EmployeesTable.C_SALARY, minSalary)
				.orderBy(EmployeesTable.C_ID)
				.asQuery();

		CompiledQuery compiled = query.compile();

		Map<String, Object> params = new HashMap<>();
		params.put(name.name(), nameFilter);
		params.put(city.name(), cityFilter);
		params.put(minSalary.name(), minSalaryFilter);

		return compiled.bind(params);
	}
}
