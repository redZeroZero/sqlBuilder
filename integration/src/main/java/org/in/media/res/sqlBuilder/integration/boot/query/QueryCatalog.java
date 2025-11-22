package org.in.media.res.sqlBuilder.integration.boot.query;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class QueryCatalog {

	private final List<QueryDefinition> definitions;
	private final Map<String, QueryDefinition> byId;

	public QueryCatalog() {
		this.definitions = List.of(
				new QueryDefinition("simple-projection", "Simple Projection",
						"Select every employee column with a deterministic ordering.",
						IntegrationQueries::simpleProjection),
				new QueryDefinition("joins-with-filters", "Joins with Filters",
						"Join jobs onto employees and filter by salary.", IntegrationQueries::joinsWithFilters),
				new QueryDefinition("aggregations", "Aggregations with GROUP BY / HAVING",
						"Aggregate job salaries per employee and filter on the computed average.",
						IntegrationQueries::aggregations),
				new QueryDefinition("pagination", "Pagination",
						"High-paying jobs ordered by salary with limit/offset.", IntegrationQueries::pagination),
				new QueryDefinition("count-employees", "Count Employees",
						"COUNT(*) on the employees table using the DSL helper.", IntegrationQueries::countEmployees),
				new QueryDefinition("active-employees", "Active Employees",
						"Filter active employees and list names alphabetically.", IntegrationQueries::activeEmployees),
				new QueryDefinition("optimizer-hints", "Optimizer Hints",
						"Inject a custom hint while filtering active employees.", IntegrationQueries::optimizerHints),
				new QueryDefinition("set-operations", "Set Operations",
						"UNION of employee and customer names.", IntegrationQueries::setOperations),
				new QueryDefinition("derived-tables", "Derived Tables",
						"Average salary per employee joined as a derived table.",
						IntegrationQueries::derivedTables),
				new QueryDefinition("cte", "Common Table Expressions",
						"AVG salary per employee declared as a CTE and reused downstream.",
						IntegrationQueries::cte),
				new QueryDefinition("subquery-filtering", "Filtering with Subqueries",
						"Filter employees by salary thresholds and job existence.",
						IntegrationQueries::subqueryFiltering),
				new QueryDefinition("optional-filters-disabled", "Optional Filters (no filters)",
						"Compiled query where optional filters are left null.", IntegrationQueries::optionalFiltersDisabled),
				new QueryDefinition("optional-filters-enabled", "Optional Filters (with filters)",
						"Compiled query with all optional filters enabled.",
						IntegrationQueries::optionalFiltersEnabled),
				new QueryDefinition("grouped-filters", "Grouped Filters",
						"Compound AND/OR groups combining location and salary windows.",
						IntegrationQueries::groupedFilters),
				new QueryDefinition("raw-sql-fragments", "Raw SQL Fragments",
						"Demonstrate inline raw fragments for aggregation and ordering.",
						IntegrationQueries::rawSqlFragments),
				new QueryDefinition("department-salary-totals", "Department Salary Totals",
						"SUM salaries and bonuses by department with a left join.",
						IntegrationQueries::departmentSalaryTotals),
				new QueryDefinition("top-paid-employees", "Top Paid Employees",
						"Highest-paid active employees ordered by salary.", IntegrationQueries::topPaidEmployees),
				new QueryDefinition("orders-with-customers", "Orders with Customers",
						"Order totals with customer names filtered by spend.",
						IntegrationQueries::ordersWithCustomers),
				new QueryDefinition("product-revenue", "Product Revenue",
						"Per-product revenue combining order lines and payments.",
						IntegrationQueries::productRevenue));

		Map<String, QueryDefinition> byId = new LinkedHashMap<>();
		for (QueryDefinition definition : definitions) {
			byId.put(definition.id(), definition);
		}
		this.byId = Map.copyOf(byId);
	}

	public List<QueryDefinition> definitions() {
		return definitions;
	}

	public QueryDefinition definition(String id) {
		return byId.get(id);
	}
}
