package org.in.media.res.sqlBuilder;

import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MAX;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MIN;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;
import static org.in.media.res.sqlBuilder.example.Employee.C_FIRST_NAME;

import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.core.query.ConditionImpl;
import org.in.media.res.sqlBuilder.core.query.FromImpl;
import org.in.media.res.sqlBuilder.core.query.QueryImpl;
import org.in.media.res.sqlBuilder.core.query.SelectImpl;
import org.in.media.res.sqlBuilder.core.query.WhereImpl;

public class MainApp {

	public static void main(String[] args) {

		String properties = System.getProperty("transpiler.class");
		System.out.println(properties);
		// properties.forEach((k, v) -> System.out.println(k + " - " + v));

		EmployeeSchema schema = new EmployeeSchema();

		schema.setName("COFSALESREPORT");
		Table e = schema.getTableBy(Employee.class);
		Table j = schema.getTableBy(Job.class);

		Select s_clause = new SelectImpl();
		s_clause.select(MAX, e.get("FIRST_NAME")).select(MIN, j.get("ID")).select(j.get("SALARY"));

		Query query = QueryImpl.newQuery();
		query.select(MAX, e.get("FIRST_NAME")).select(MIN, j.get(Job.C_ID)).select(j.get(Job.C_SALARY));

		System.out.println("SELECT OBJECT -> " + s_clause.transpile());
		System.out.println("QUERY OBJECT  -> " + s_clause.transpile());

		System.out.println("----------------------------------------------------------------------------");

		From f_clause = new FromImpl();
		f_clause.from(e).join(j).on(e.get(C_FIRST_NAME), j.get(Job.C_SALARY));
		System.out.println("FROM OBJECT -> " + f_clause.transpile());
		query.from(e).join(j).on(e.get(C_FIRST_NAME), j.get(Job.C_SALARY));
		System.out.println("QUERY OBJECT -> " + query.transpile());

		Condition c = ConditionImpl.builder().and().leftColumn(e.get(C_FIRST_NAME)).comparisonOp(EQ)
				.rightColumn(j.get(Job.C_ID)).build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());

		c = ConditionImpl.builder().and().leftColumn(MAX, e.get(C_FIRST_NAME)).comparisonOp(EQ).value("Tagada").build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());
		c = ConditionImpl.builder().and().leftColumn(MAX, e.get(C_FIRST_NAME)).comparisonOp(EQ).value(15).build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());
		c = ConditionImpl.builder().and().leftColumn(MAX, e.get(C_FIRST_NAME)).comparisonOp(EQ).values(15, 12, 78, 35)
				.build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());
		c = ConditionImpl.builder().and().leftColumn(MAX, e.get(C_FIRST_NAME)).comparisonOp(EQ)
				.values("15", "12", "78", "35").build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());

		Where where = new WhereImpl();
		where.where(e.get("FIRST_NAME")).in("Tagada", "tsoin").and(e.get(C_FIRST_NAME)).eq("ULUBERLU").or()
				.max(e.get(C_FIRST_NAME)).eq().sum(j.get("ID")).condition(c);
		System.out.println(where.transpile());

		Query q = QueryImpl.newQuery();
		q.select(e).select(j).from(e).join(j).on(e.get("ID"), j.get("EMPLOYEE_ID")).where(e.get(C_FIRST_NAME))
				.eq("Alphonse");

		System.out.println(q.transpile());

		Query chained = QueryImpl.newQuery();
		chained.select(e);
		chained.join(j);
		chained.on(Employee.C_ID, Job.C_EMPLOYEE_ID);
		chained.where(C_FIRST_NAME).eq("NAME");
		System.out.println(chained.transpile());

		Query rqt = QueryImpl.newQuery().select(e).join(j).on(Employee.C_ID, Job.C_EMPLOYEE_ID).where(Employee.C_ID).eq("John");
		System.out.println(rqt.transpile());

	 	System.out.println(QueryImpl.countAll().from(e).transpile());
	}

}
