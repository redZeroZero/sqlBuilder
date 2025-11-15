package org.in.media.res.sqlBuilder.examples.internal;

import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.constants.SortDirection;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;

/**
 * INTERNAL DEMO / BENCHMARK.
 * <p>
 * These utilities are not part of the supported sqlBuilder API and may change or be
 * removed without notice.
 */
public final class QueryBenchmark {

	private QueryBenchmark() {
	}

	public static void main(String[] args) {
		int iterations = args.length > 0 ? Integer.parseInt(args[0]) : 10000;

		EmployeeSchema schema = new EmployeeSchema();
		var employee = schema.getTableBy(Employee.class);
		var job = schema.getTableBy(Job.class);

		Query query = SqlQuery.query();
		query.select(Employee.C_FIRST_NAME, Employee.C_LAST_NAME)
				.select(AggregateOperator.AVG, Job.C_SALARY)
				.from(employee)
				.innerJoin(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID);
		query.where(Job.C_SALARY).supOrEqTo(50000);
		query.groupBy(Employee.C_FIRST_NAME, Employee.C_LAST_NAME);

		query.having(Job.C_SALARY).avg(Job.C_SALARY).supOrEqTo(60000);
		query.orderBy(employee.get(Employee.C_LAST_NAME), SortDirection.DESC)
				.limitAndOffset(25, 0);

		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			query.transpile();
		}
		long elapsed = System.nanoTime() - start;
		double perQueryMicros = (elapsed / 1_000.0) / iterations;

		System.out.printf("Transpiled %,d queries in %.2f ms (%.3f µs/query)%n",
				iterations, elapsed / 1_000_000.0, perQueryMicros);
	}
}
