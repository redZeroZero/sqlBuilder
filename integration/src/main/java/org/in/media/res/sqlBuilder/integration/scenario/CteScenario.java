package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CteRef;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.WithBuilder;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.JobsTable;

public final class CteScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "8. Common Table Expressions";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		Table jobs = IntegrationSchema.jobs();

		Query avgSalary = SqlQuery.newQuery()
				.select(EmployeesTable.C_ID)
				.select(AggregateOperator.AVG, JobsTable.C_SALARY)
				.from(employees)
				.join(jobs).on(EmployeesTable.C_ID, JobsTable.C_EMPLOYEE_ID)
				.groupBy(EmployeesTable.C_ID)
				.asQuery();

		// Demonstrates the chained CTE style; other scenarios still use cte(...) directly.
		WithBuilder.CteStep salaryAvgStep = SqlQuery.withCte("salary_avg").as(avgSalary, "EMPLOYEE_ID", "AVG_SALARY");
		CteRef salaryAvg = salaryAvgStep.ref();

		Query main = SqlQuery.newQuery()
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.join(salaryAvg).on(EmployeesTable.C_ID, salaryAvg.column("EMPLOYEE_ID"))
				.where(salaryAvg.column("AVG_SALARY")).supOrEqTo(85_000)
				.asQuery();

		ScenarioSupport.executeQuery(connection, salaryAvgStep.and().main(main).render(), title());
	}
}
