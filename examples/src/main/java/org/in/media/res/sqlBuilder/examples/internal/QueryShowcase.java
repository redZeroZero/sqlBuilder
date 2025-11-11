package org.in.media.res.sqlBuilder.examples.internal;

import static org.in.media.res.sqlBuilder.constants.AggregateOperator.AVG;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MAX;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MIN;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.api.query.spi.Select;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;

/**
 * INTERNAL DEMO / BENCHMARK.
 * <p>
 * Utility for manual exploration only; not part of the supported sqlBuilder API.
 */
public final class QueryShowcase {

    private QueryShowcase() {
    }

    public static void main(String[] args) {
        EmployeeSchema schema = new EmployeeSchema();
        Table employee = schema.getTableBy(Employee.class);
        Table job = schema.getTableBy(Job.class);

        demoSelectClause(employee, job);
        demoQueryDsl(employee, job);
        demoDerivedTables(employee, job);
        demoSubqueryPredicates(employee, job);
    }

    private static void demoSelectClause(Table employee, Table job) {
        Select select = SqlQuery.newQuery();
        select.select(MAX, Employee.C_FIRST_NAME)
              .select(MIN, Job.C_ID)
              .select(Job.C_SALARY);
        System.out.println("SELECT CLAUSE -> " + select.transpile());
    }

    private static void demoQueryDsl(Table employee, Table job) {
        Query query = SqlQuery.newQuery().asQuery();
        query.select(employee)
                .from(employee)
                .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
                .where(Employee.C_FIRST_NAME).eq("Alphonse");
        System.out.println("BASIC QUERY -> " + query.transpile());

        Query count = SqlQuery.countAll()
                .from(employee)
                .asQuery();
        System.out.println("COUNT QUERY -> " + count.transpile());
    }

    private static void demoDerivedTables(Table employee, Table job) {
        Query salarySummary = SqlQuery.newQuery().asQuery();
        salarySummary.select(Employee.C_ID)
                .select(AVG, Job.C_SALARY)
                .from(employee)
                .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
                .groupBy(Employee.C_ID);

        Table salaryAvg = SqlQuery.toTable(salarySummary, "SALARY_AVG", "EMPLOYEE_ID", "AVG_SALARY");

        Query derived = SqlQuery.newQuery().asQuery();
        derived.select(Employee.C_FIRST_NAME)
                .from(employee)
                .join(salaryAvg).on(Employee.C_ID, salaryAvg.get("EMPLOYEE_ID"))
                .where(salaryAvg.get("AVG_SALARY")).supOrEqTo(60000);

        System.out.println("DERIVED TABLE -> " + derived.transpile());
    }

    private static void demoSubqueryPredicates(Table employee, Table job) {
        Query highSalaryIds = SqlQuery.newQuery().asQuery();
        highSalaryIds.select(Job.C_EMPLOYEE_ID)
                .from(job)
                .where(Job.C_SALARY).supOrEqTo(60000);

        Query inSubquery = SqlQuery.newQuery().asQuery();
        inSubquery.select(Employee.C_FIRST_NAME)
                .from(employee)
                .where(Employee.C_ID).in(highSalaryIds);
        System.out.println("IN SUBQUERY -> " + inSubquery.transpile());

        Query existsSubquery = SqlQuery.newQuery().asQuery();
        existsSubquery.select(Employee.C_FIRST_NAME)
                .from(employee)
                .exists(SqlQuery.newQuery().select(Job.C_ID).from(job).asQuery());
        System.out.println("EXISTS SUBQUERY -> " + existsSubquery.transpile());

        // Condition demonstration using the fluent DSL
        Query whereDemo = SqlQuery.newQuery().asQuery();
        whereDemo.select(Employee.C_FIRST_NAME)
                .from(employee)
                .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID);
        whereDemo.where(Employee.C_FIRST_NAME)
                .in("Tagada", "tsoin")
                .and(Employee.C_FIRST_NAME)
                .eq("ULUBERLU")
                .and(Job.C_ID).eq(42);
        System.out.println("WHERE CHAIN -> " + whereDemo.transpile());
    }
}
