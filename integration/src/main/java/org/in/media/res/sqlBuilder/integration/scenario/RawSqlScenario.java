package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;

public final class RawSqlScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "12. Raw SQL Fragments";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Query query = SqlQuery.query();
		query.selectRaw("c.first_name || ' ' || c.last_name AS customer_name");
		query.selectRaw("SUM(o.total) AS order_total");
		query.fromRaw("customers c");
		query.joinRaw("orders o ON o.customer_id = c.id");
		query.whereRaw("o.status <> 'PENDING'");
		query.groupByRaw("c.first_name, c.last_name");
		query.havingRaw("SUM(o.total) > 300");
		query.orderByRaw("order_total DESC");

		ScenarioSupport.executeQuery(connection, query.render(), title());
	}
}
