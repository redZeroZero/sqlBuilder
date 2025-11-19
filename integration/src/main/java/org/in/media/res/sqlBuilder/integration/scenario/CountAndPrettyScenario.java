package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.EmployeesTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class CountAndPrettyScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "5. Count & Pretty Print";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();

		Query countQuery = SqlQuery.countAll()
				.from(employees)
				.asQuery();
		ScenarioSupport.executeQuery(connection, countQuery.render(), title() + " (count)");

		Query printable = SqlQuery.newQuery()
				.select(EmployeesTable.C_FIRST_NAME)
				.select(EmployeesTable.C_LAST_NAME)
				.from(employees)
				.asQuery();
		printable.where(EmployeesTable.C_STATUS).eq("ACTIVE");

		System.out.println(printable.prettyPrint());
		ScenarioSupport.executeQuery(connection, printable.render(), title() + " (filtered list)");
	}
}
