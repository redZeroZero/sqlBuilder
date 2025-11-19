package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.QueryHelper;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.DepartmentsTable;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class GroupedFiltersScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "11. Grouped Filters";
	}

	@Override
	public void run(Connection connection) throws SQLException {
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
				.and(salaryGroup);

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
