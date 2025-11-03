package org.in.media.res.sqlBuilder.tools;

import static org.in.media.res.sqlBuilder.constants.AggregateOperator.AVG;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MAX;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MIN;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.core.query.ConditionImpl;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;
import org.in.media.res.sqlBuilder.core.query.SelectImpl;
import org.in.media.res.sqlBuilder.core.query.WhereImpl;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;

/**
 * Lightweight showcase that prints representative queries to stdout.
 */
public final class QueryShowcase {

    private QueryShowcase() {
    }

    public static void main(String[] args) {
        EmployeeSchema schema = new EmployeeSchema();
        Table employee = schema.getTableBy(Employee.class);
        Table job = schema.getTableBy(Job.class);

        demoSelectClause(employee, job);
        demoQueryBuilder(employee, job);
        demoDerivedTables(employee, job);
        demoSubqueryPredicates(employee, job);
    }

    private static void demoSelectClause(Table employee, Table job) {
        Select select = new SelectImpl();
        select.select(MAX, employee.get("FIRST_NAME"))
              .select(MIN, job.get("ID"))
              .select(job.get("SALARY"));
        System.out.println("SELECT CLAUSE -> " + select.transpile());
    }

    private static void demoQueryBuilder(Table employee, Table job) {
        Query query = QueryImpl.newQuery()
                .select(employee)
                .from(employee)
                .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
                .where(Employee.C_FIRST_NAME).eq("Alphonse");
        System.out.println("BASIC QUERY -> " + query.transpile());

        Query count = QueryImpl.countAll().from(employee);
        System.out.println("COUNT QUERY -> " + count.transpile());
    }

    private static void demoDerivedTables(Table employee, Table job) {
        Query salarySummary = QueryImpl.newQuery()
                .select(Employee.C_ID)
                .select(AVG, job.get(Job.C_SALARY))
                .from(employee)
                .join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
                .groupBy(Employee.C_ID);

        Table salaryAvg = QueryImpl.toTable(salarySummary, "SALARY_AVG", "EMPLOYEE_ID", "AVG_SALARY");

        Query derived = QueryImpl.newQuery()
                .select(Employee.C_FIRST_NAME)
                .from(employee)
                .join(salaryAvg).on(employee.get(Employee.C_ID), salaryAvg.get("EMPLOYEE_ID"))
                .where(salaryAvg.get("AVG_SALARY")).supOrEqTo(60000);

        System.out.println("DERIVED TABLE -> " + derived.transpile());
    }

    private static void demoSubqueryPredicates(Table employee, Table job) {
        Query highSalaryIds = QueryImpl.newQuery()
                .select(job.get(Job.C_EMPLOYEE_ID))
                .from(job)
                .where(job.get(Job.C_SALARY)).supOrEqTo(60000);

        Query inSubquery = QueryImpl.newQuery()
                .select(employee.get(Employee.C_FIRST_NAME))
                .from(employee)
                .where(employee.get(Employee.C_ID)).in(highSalaryIds);
        System.out.println("IN SUBQUERY -> " + inSubquery.transpile());

        Query existsSubquery = QueryImpl.newQuery()
                .select(employee.get(Employee.C_FIRST_NAME))
                .from(employee)
                .exists(QueryImpl.newQuery().select(job.get(Job.C_ID)).from(job));
        System.out.println("EXISTS SUBQUERY -> " + existsSubquery.transpile());

        // Condition demonstration
        Where where = new WhereImpl();
        ConditionImpl condition = ConditionImpl.builder()
                .and()
                .leftColumn(employee.get(Employee.C_FIRST_NAME))
                .comparisonOp(EQ)
                .rightColumn(job.get(Job.C_ID))
                .build();
        where.where(employee.get("FIRST_NAME"))
                .in("Tagada", "tsoin")
                .and(employee.get(Employee.C_FIRST_NAME))
                .eq("ULUBERLU");
        where.condition(condition);
        System.out.println("WHERE CHAIN -> " + where.transpile());
    }
}
