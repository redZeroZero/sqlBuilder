package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.SqlParameters;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.PaymentsTable;

public final class InsertScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "14. Inserts (DML)";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		boolean autoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		try {
			executeInsert(connection);
			connection.rollback();
		} finally {
			connection.setAutoCommit(autoCommit);
		}
	}

	private void executeInsert(Connection connection) throws SQLException {
		Table payments = IntegrationSchema.payments();
		SqlParameter<Long> orderId = SqlParameters.param("orderId");
		SqlParameter<Integer> amount = SqlParameters.param("amount");
		SqlParameter<String> method = SqlParameters.param("method");

		CompiledQuery compiled = SqlQuery.insertInto(payments)
				.columns(PaymentsTable.C_ORDER_ID, PaymentsTable.C_AMOUNT, PaymentsTable.C_METHOD)
				.values(orderId, amount, method)
				.compile();

		Map<String, Object> params = new HashMap<>();
		params.put(orderId.name(), 1L);
		params.put(amount.name(), 100);
		params.put(method.name(), "CARD");

		ScenarioSupport.executeUpdate(connection, compiled.bind(params), title());
	}
}
