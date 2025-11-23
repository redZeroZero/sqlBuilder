package org.in.media.res.sqlBuilder.integration.boot.config;

import org.in.media.res.sqlBuilder.integration.boot.query.IntegrationQueries;
import org.in.media.res.sqlBuilder.integration.boot.query.QueryDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoQueryRepository {

	@Bean
	public QueryDefinition simpleProjectionQuery() {
		return new QueryDefinition("simple-projection", "Simple Projection",
				"Select every employee column with a deterministic ordering.",
				IntegrationQueries::simpleProjection);
	}

	@Bean
	public QueryDefinition joinsWithFiltersQuery() {
		return new QueryDefinition("joins-with-filters", "Joins with Filters",
				"Join jobs onto employees and filter by salary.", IntegrationQueries::joinsWithFilters);
	}

	@Bean
	public QueryDefinition aggregationsQuery() {
		return new QueryDefinition("aggregations", "Aggregations with GROUP BY / HAVING",
				"Aggregate job salaries per employee and filter on the computed average.",
				IntegrationQueries::aggregations);
	}

	@Bean
	public QueryDefinition paginationQuery() {
		return new QueryDefinition("pagination", "Pagination",
				"High-paying jobs ordered by salary with limit/offset.", IntegrationQueries::pagination);
	}

	@Bean
	public QueryDefinition countEmployeesQuery() {
		return new QueryDefinition("count-employees", "Count Employees",
				"COUNT(*) on the employees table using the DSL helper.", IntegrationQueries::countEmployees);
	}

	@Bean
	public QueryDefinition activeEmployeesQuery() {
		return new QueryDefinition("active-employees", "Active Employees",
				"Filter active employees and list names alphabetically.", IntegrationQueries::activeEmployees);
	}

	@Bean
	public QueryDefinition optimizerHintsQuery() {
		return new QueryDefinition("optimizer-hints", "Optimizer Hints",
				"Inject a custom hint while filtering active employees.", IntegrationQueries::optimizerHints);
	}

	@Bean
	public QueryDefinition setOperationsQuery() {
		return new QueryDefinition("set-operations", "Set Operations",
				"UNION of employee and customer names.", IntegrationQueries::setOperations);
	}

	@Bean
	public QueryDefinition derivedTablesQuery() {
		return new QueryDefinition("derived-tables", "Derived Tables",
				"Average salary per employee joined as a derived table.",
				IntegrationQueries::derivedTables);
	}

	@Bean
	public QueryDefinition cteQuery() {
		return new QueryDefinition("cte", "Common Table Expressions",
				"AVG salary per employee declared as a CTE and reused downstream.",
				IntegrationQueries::cte);
	}

	@Bean
	public QueryDefinition subqueryFilteringQuery() {
		return new QueryDefinition("subquery-filtering", "Filtering with Subqueries",
				"Filter employees by salary thresholds and job existence.",
				IntegrationQueries::subqueryFiltering);
	}

	@Bean
	public QueryDefinition optionalFiltersDisabledQuery() {
		return new QueryDefinition("optional-filters-disabled", "Optional Filters (no filters)",
				"Compiled query where optional filters are left null.", IntegrationQueries::optionalFiltersDisabled);
	}

	@Bean
	public QueryDefinition optionalFiltersEnabledQuery() {
		return new QueryDefinition("optional-filters-enabled", "Optional Filters (with filters)",
				"Compiled query with all optional filters enabled.",
				IntegrationQueries::optionalFiltersEnabled);
	}

	@Bean
	public QueryDefinition groupedFiltersQuery() {
		return new QueryDefinition("grouped-filters", "Grouped Filters",
				"Compound AND/OR groups combining location and salary windows.",
				IntegrationQueries::groupedFilters);
	}

	@Bean
	public QueryDefinition rawSqlFragmentsQuery() {
		return new QueryDefinition("raw-sql-fragments", "Raw SQL Fragments",
				"Demonstrate inline raw fragments for aggregation and ordering.",
				IntegrationQueries::rawSqlFragments);
	}

	@Bean
	public QueryDefinition departmentSalaryTotalsQuery() {
		return new QueryDefinition("department-salary-totals", "Department Salary Totals",
				"SUM salaries and bonuses by department with a left join.",
				IntegrationQueries::departmentSalaryTotals);
	}

	@Bean
	public QueryDefinition topPaidEmployeesQuery() {
		return new QueryDefinition("top-paid-employees", "Top Paid Employees",
				"Highest-paid active employees ordered by salary.", IntegrationQueries::topPaidEmployees);
	}

	@Bean
	public QueryDefinition ordersWithCustomersQuery() {
		return new QueryDefinition("orders-with-customers", "Orders with Customers",
				"Order totals with customer names filtered by spend.",
				IntegrationQueries::ordersWithCustomers);
	}

	@Bean
	public QueryDefinition productRevenueQuery() {
		return new QueryDefinition("product-revenue", "Product Revenue",
				"Per-product revenue combining order lines and payments.",
				IntegrationQueries::productRevenue);
	}

	@Bean
	public QueryDefinition topEarnersByDepartmentQuery() {
		return new QueryDefinition("top-earners-by-department", "Top Earners by Department",
				"Window function to rank salaries per department and pick the top earner.",
				IntegrationQueries::topEarnersByDepartment);
	}

	@Bean
	public QueryDefinition customerOrderSummariesQuery() {
		return new QueryDefinition("customer-order-summaries", "Customer Order Summaries",
				"Aggregate order totals and payments per customer with HAVING filter.",
				IntegrationQueries::customerOrderSummaries);
	}

	@Bean
	public QueryDefinition aboveDepartmentAverageQuery() {
		return new QueryDefinition("above-department-average", "Above Department Average",
				"Correlated subquery to find employees earning above their department average.",
				IntegrationQueries::aboveDepartmentAverage);
	}
}
