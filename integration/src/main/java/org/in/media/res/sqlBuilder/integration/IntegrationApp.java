package org.in.media.res.sqlBuilder.integration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.integration.model.IntegrationTables;

public final class IntegrationApp {

    public static void main(String[] args) throws Exception {
        IntegrationConfig config = IntegrationConfig.fromEnvironment();
        System.out.printf("Connecting to %s as %s%n", config.jdbcUrl(), config.user());
        try (Connection connection = config.connect()) {
            describeServer(connection);
            runEmployeeQuery(connection);
            runOrderSummary(connection);
        }
    }

    private static void describeServer(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT version()")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("PostgreSQL version: " + rs.getString(1));
                }
            }
        }
    }

    private static void runEmployeeQuery(Connection connection) throws SQLException {
        Table employees = IntegrationTables.EMPLOYEES;
        Table departments = IntegrationTables.DEPARTMENTS;

        Query query = SqlQuery.newQuery().asQuery();
        query.select(employees.get("first_name"))
                .select(employees.get("last_name"))
                .select(departments.get("name"))
                .select(employees.get("salary"))
                .from(employees)
                .join(departments).on(employees.get("department_id"), departments.get("id"))
                .where(employees.get("salary")).supOrEqTo(85000)
                .orderBy(employees.get("last_name"));

        executeAndPrint(connection, query.render(), "High-earners by Department");
    }

    private static void runOrderSummary(Connection connection) throws SQLException {
        Table customers = IntegrationTables.CUSTOMERS;
        Table orders = IntegrationTables.ORDERS;

        Query query = SqlQuery.newQuery().asQuery();
        query.select(customers.get("first_name"))
                .select(customers.get("last_name"))
                .select(AggregateOperator.SUM, orders.get("total"))
                .from(customers)
                .join(orders).on(customers.get("id"), orders.get("customer_id"))
                .groupBy(customers.get("first_name"))
                .groupBy(customers.get("last_name"))
                .having(orders.get("total")).sum(orders.get("total")).supOrEqTo(500)
                .orderBy(customers.get("last_name"));

        executeAndPrint(connection, query.render(), "Customers with large orders");
    }

    private static void executeAndPrint(Connection connection, SqlAndParams andParams, String title) throws SQLException {
        System.out.printf("\n== %s ==%n", title);
        System.out.println(andParams.sql());
        try (PreparedStatement stmt = connection.prepareStatement(andParams.sql())) {
            List<Object> params = andParams.params();
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet result = stmt.executeQuery()) {
                ResultSetMetaData meta = result.getMetaData();
                while (result.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        if (i > 1) {
                            row.append(" | ");
                        }
                        row.append(meta.getColumnLabel(i)).append("=").append(result.getObject(i));
                    }
                    System.out.println(row);
                }
            }
        }
    }

    private IntegrationApp() {
    }
}
