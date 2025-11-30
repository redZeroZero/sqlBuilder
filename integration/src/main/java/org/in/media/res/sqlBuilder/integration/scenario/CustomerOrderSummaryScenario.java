package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.SQLException;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.integration.model.CustomersTable;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.in.media.res.sqlBuilder.integration.model.OrdersTable;
import org.in.media.res.sqlBuilder.integration.model.PaymentsTable;

public final class CustomerOrderSummaryScenario implements IntegrationScenario {

	@Override
	public String title() {
		return "17. Customer order summaries";
	}

	@Override
	public void run(Connection connection) throws SQLException {
		Table customers = IntegrationSchema.customers();
		Table orders = IntegrationSchema.orders();
		Table payments = IntegrationSchema.payments();

		Query aggregated = SqlQuery.query()
				.select(CustomersTable.C_FIRST_NAME)
				.select(CustomersTable.C_LAST_NAME)
				.selectRaw("SUM(o.total) AS total_sum")
				.selectRaw("SUM(pay.amount) AS paid_sum")
				.from(customers)
				.join(orders).on(CustomersTable.C_ID, OrdersTable.C_CUSTOMER_ID)
				.leftJoin(payments).on(PaymentsTable.C_ORDER_ID, OrdersTable.C_ID)
				.groupBy(CustomersTable.C_FIRST_NAME, CustomersTable.C_LAST_NAME)
				.having(OrdersTable.C_TOTAL).sum(OrdersTable.C_TOTAL).supTo(300)
				.orderByAlias("total_sum", SortDirection.DESC);

		ScenarioSupport.executeQuery(connection, aggregated.render(), title());
	}
}
