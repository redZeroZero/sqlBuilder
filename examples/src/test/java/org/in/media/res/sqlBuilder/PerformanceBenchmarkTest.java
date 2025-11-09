package org.in.media.res.sqlBuilder;

import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;
import org.junit.jupiter.api.Test;

/**
 * Lightweight performance smoke tests. These are not strict assertions but
 * provide timing information during CI runs to keep an eye on regressions.
 */
class PerformanceBenchmarkTest {

	@Test
	void queryBuilderThroughput() {
		int iterations = Integer.getInteger("sqlbuilder.perf.iterations", 20_000);

		EmployeeSchema schema = new EmployeeSchema();
		var employee = schema.getTableBy(Employee.class);
		var job = schema.getTableBy(Job.class);

		long start = System.nanoTime();
		for (int i = 0; i < iterations; i++) {
			SqlQuery.newQuery()
					.select(Employee.C_FIRST_NAME, Employee.C_LAST_NAME)
					.select(org.in.media.res.sqlBuilder.constants.AggregateOperator.AVG, Job.C_SALARY)
					.from(employee)
					.join(job).on(Employee.C_ID, Job.C_EMPLOYEE_ID)
					.where(Job.C_SALARY).supOrEqTo(50_000)
					.groupBy(Employee.C_FIRST_NAME, Employee.C_LAST_NAME)
					.orderBy(Employee.C_LAST_NAME)
					.limitAndOffset(25, 0)
					.transpile();
		}
		long elapsed = System.nanoTime() - start;
		double perQueryMicros = (elapsed / 1_000.0) / iterations;
		System.out.printf("Query DSL throughput: %,d iterations in %.2f ms (%.3f µs/query)%n",
				iterations, elapsed / 1_000_000.0, perQueryMicros);
	}

	@Test
	void mainAppStartupTiming() {
		long start = System.nanoTime();
		MainApp.main(new String[0]);
		long elapsed = System.nanoTime() - start;
		System.out.printf("MainApp startup + execution: %.2f ms%n", elapsed / 1_000_000.0);
	}
}
