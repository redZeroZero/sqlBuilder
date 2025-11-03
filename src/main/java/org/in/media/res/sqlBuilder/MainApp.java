package org.in.media.res.sqlBuilder;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.QueryHelper;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;

public final class MainApp {

	private MainApp() {
	}

	public static void main(String[] args) {
		EmployeeSchema schema = new EmployeeSchema();
		Table employee = schema.getTableBy(Employee.class);
		Table job = schema.getTableBy(Job.class);

		Query salarySummary = QueryImpl.newQuery()
				.select(Employee.C_ID)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(employee)
				.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
				.groupBy(Employee.C_ID);

		Table salaryAverages = QueryImpl.toTable(salarySummary, "SALARY_AVG", "EMPLOYEE_ID", "AVG_SALARY");

		String sql = QueryImpl.newQuery()
				.select(Employee.C_FIRST_NAME)
				.select(Employee.C_LAST_NAME)
				.from(employee)
				.join(salaryAverages).on(employee.get(Employee.C_ID), salaryAverages.get("EMPLOYEE_ID"))
				.where(QueryHelper.group(group -> group
						.where(salaryAverages.get("AVG_SALARY")).supOrEqTo(90_000)))
				.and(QueryHelper.group(group -> group
						.where(Employee.C_LAST_NAME).like("M%")
						.or(Employee.C_MAIL).like("%@acme.com")))
				.orderBy(Employee.C_LAST_NAME)
				.prettyPrint();

		System.out.println(sql);
	}
}
