package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.DepartmentsTable;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.JobsTable;


public final class TopEarnersByDepartmentScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "16. Top earners per department (window)";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		Table departments = IntegrationSchema.departments();
		Table jobs = IntegrationSchema.jobs();

		Query ranking = SqlQuery.newQuery()
				.select(DepartmentsTable.C_NAME)
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.select(JobsTable.C_SALARY)
				.selectRaw("ROW_NUMBER() OVER(PARTITION BY d.id ORDER BY j.salary DESC) AS rn")
				.from(employees)
				.join(departments).on(EmployeesTable.C_DEPARTMENT_ID, DepartmentsTable.C_ID)
				.join(jobs).on(EmployeesTable.C_ID, JobsTable.C_EMPLOYEE_ID)
				.asQuery();

		Table ranked = SqlQuery.toTable(ranking, "ranked", "name", "firstName", "lastName", "salary", "rn");

		Query top = SqlQuery.query();
		top.select(ranked.get("name"))
				.select(ranked.get("firstName"))
				.select(ranked.get("lastName"))
				.select(ranked.get("salary"))
				.from(ranked)
				.where(ranked.get("rn")).eq(1)
				.orderBy(ranked.get("name"));

		ScenarioSupport.executeQuery(connection, top.render(), title());
	}
}
