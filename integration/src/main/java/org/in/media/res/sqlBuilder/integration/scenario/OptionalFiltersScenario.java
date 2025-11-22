package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.DepartmentsTable;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class OptionalFiltersScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "10. Optional Filters";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		Table departments = IntegrationSchema.departments();

		SqlParameter<String> name = SqlParameters.param("nameFilter", String.class);
		SqlParameter<String> city = SqlParameters.param("cityFilter", String.class);
		SqlParameter<Integer> minSalary = SqlParameters.param("minSalary", Integer.class);

		Query query = SqlQuery.newQuery()
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.select(DepartmentsTable.C_NAME)
				.from(employees)
				.join(departments).on(EmployeesTable.C_DEPARTMENT_ID, DepartmentsTable.C_ID)
				.whereOptionalEquals(EmployeesTable.C_FIRST_NAME, name)
				.whereOptionalEquals(DepartmentsTable.C_LOCATION, city)
				.whereOptionalGreaterOrEqual(EmployeesTable.C_SALARY, minSalary)
				.asQuery();

		CompiledQuery compiled = query.compile();

		Map<String, Object> disabled = new HashMap<>();
		disabled.put(name.name(), null);
		disabled.put(city.name(), null);
		disabled.put(minSalary.name(), null);
		ScenarioSupport.executeQuery(connection, compiled.bind(disabled), title() + " (no filters)");

		Map<String, Object> enabled = new HashMap<>();
		enabled.put(name.name(), "Alice");
		enabled.put(city.name(), "Montreal");
		enabled.put(minSalary.name(), 90_000);
		ScenarioSupport.executeQuery(connection, compiled.bind(enabled), title() + " (with filters)");
	}
}
