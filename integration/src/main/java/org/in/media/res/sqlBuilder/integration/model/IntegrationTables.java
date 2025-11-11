package org.in.media.res.sqlBuilder.integration.model;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;

public final class IntegrationTables {

    public static final Table EMPLOYEES = Tables.builder("employees", "e")
            .column("id")
            .column("first_name")
            .column("last_name")
            .column("department_id")
            .column("hire_date")
            .column("salary")
            .build();

    public static final Table DEPARTMENTS = Tables.builder("departments", "d")
            .column("id")
            .column("name")
            .build();

    public static final Table JOBS = Tables.builder("jobs", "j")
            .column("id")
            .column("title")
            .column("salary")
            .column("employee_id")
            .build();

    public static final Table CUSTOMERS = Tables.builder("customers", "c")
            .column("id")
            .column("first_name")
            .column("last_name")
            .column("email")
            .column("created_at")
            .build();

    public static final Table ORDERS = Tables.builder("orders", "o")
            .column("id")
            .column("customer_id")
            .column("order_date")
            .column("total")
            .build();

    public static final Table ORDER_LINES = Tables.builder("order_lines", "ol")
            .column("id")
            .column("order_id")
            .column("product_id")
            .column("quantity")
            .column("unit_price")
            .build();

    public static final Table PRODUCTS = Tables.builder("products", "p")
            .column("id")
            .column("name")
            .column("category")
            .column("price")
            .build();

    public static final Table PAYMENTS = Tables.builder("payments", "pay")
            .column("id")
            .column("order_id")
            .column("amount")
            .column("paid_at")
            .build();

    private IntegrationTables() {
    }
}
