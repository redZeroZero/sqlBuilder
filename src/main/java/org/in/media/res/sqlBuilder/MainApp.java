package org.in.media.res.sqlBuilder;

import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MAX;
import static org.in.media.res.sqlBuilder.constants.AggregateOperator.MIN;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;
import static org.in.media.res.sqlBuilder.example.Employee.C_FIRST_NAME;

import java.util.Properties;

import org.in.media.res.sqlBuilder.example.Employee;
import org.in.media.res.sqlBuilder.example.EmployeeSchema;
import org.in.media.res.sqlBuilder.example.Job;
import org.in.media.res.sqlBuilder.implementation.Condition;
import org.in.media.res.sqlBuilder.implementation.From;
import org.in.media.res.sqlBuilder.implementation.Query;
import org.in.media.res.sqlBuilder.implementation.Select;
import org.in.media.res.sqlBuilder.implementation.Where;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.IFrom;
import org.in.media.res.sqlBuilder.interfaces.query.IQuery;
import org.in.media.res.sqlBuilder.interfaces.query.ISelect;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;

public class MainApp {

	public static void main(String[] args) {

		String properties = System.getProperty("transpiler.class");
		System.out.println(properties);
		//properties.forEach((k, v) -> System.out.println(k + " - " + v));

		EmployeeSchema schema = new EmployeeSchema();

		schema.setName("COFSALESREPORT");
		ITable e = schema.getTableBy(Employee.class);
		ITable j = schema.getTableBy(Job.class);

		ISelect s_clause = new Select();
		s_clause.select(MAX, e.get("FIRST_NAME")).select(MIN, j.get("ID")).select(j.get("SALARY"));

        IQuery query = Query.newQuery();
		query.select(MAX, e.get("FIRST_NAME")).select(MIN, j.get(Job.C_ID)).select(j.get(Job.C_SALARY));

		System.out.println("SELECT OBJECT -> " + s_clause.transpile());
		System.out.println("QUERY OBJECT  -> " + s_clause.transpile());

		System.out.println("----------------------------------------------------------------------------");

		IFrom f_clause = new From();
		f_clause.from(e).join(j).on(e.get(C_FIRST_NAME), j.get(Job.C_SALARY));
		System.out.println("FROM OBJECT -> " + f_clause.transpile());
		query.from(e).join(j).on(e.get(C_FIRST_NAME), j.get(Job.C_SALARY));
		System.out.println("QUERY OBJECT -> " + query.transpile());

		Condition c = Condition.builder().and().leftColumn(e.get(C_FIRST_NAME)).comparisonOp(EQ)
				.rightColumn(j.get(Job.C_ID)).build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());

		c = Condition.builder().and().leftColumn(MAX, e.get(C_FIRST_NAME)).comparisonOp(EQ).value("Tagada").build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());
		c = Condition.builder().and().leftColumn(MAX, e.get(C_FIRST_NAME)).comparisonOp(EQ).value(15).build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());
		c = Condition.builder().and().leftColumn(MAX, e.get(C_FIRST_NAME)).comparisonOp(EQ).values(15, 12, 78, 35)
				.build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());
		c = Condition.builder().and().leftColumn(MAX, e.get(C_FIRST_NAME)).comparisonOp(EQ)
				.values("15", "12", "78", "35").build();
		System.out.println("CONDITION OBJECT -> " + c.transpile());

		IWhere where = new Where();
		where.where(e.get("FIRST_NAME")).in("Tagada", "tsoin").and(e.get(C_FIRST_NAME)).eq("ULUBERLU").or()
				.max(e.get(C_FIRST_NAME)).eq().sum(j.get("ID")).condition(c);
		System.out.println(where.transpile());

        IQuery q = Query.newQuery();
		q.select(e).select(j).from(e).join(j).on(e.get("ID"), j.get("EMPLOYEE_ID")).where(e.get(C_FIRST_NAME))
				.eq("Alphonse");

		System.out.println(q.transpile());

	}

}
