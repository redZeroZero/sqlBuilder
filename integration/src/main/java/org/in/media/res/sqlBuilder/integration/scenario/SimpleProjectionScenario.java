package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class SimpleProjectionScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "1. Simple Projection";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table employees = IntegrationSchema.employees();
		Query query = SqlQuery.query();
		query.select(employees)
				.from(employees);
		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
