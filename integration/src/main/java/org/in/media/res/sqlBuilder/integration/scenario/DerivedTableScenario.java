package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.JobsTable;

public final class DerivedTableScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "7. Derived Tables";
	}

	@Override
	public void run(Connection connection) throws SQLException {
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
				.where(salaryAvg.get("AVG_SALARY")).supOrEqTo(80_000);

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
