package org.in.media.res.sqlBuilder.api.query.spi;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;

public interface Comparator {

	public Comparator condition(Condition condition);

	public Comparator where(Column column);

	public Aggregator eq();

	public Aggregator supTo();

	public Aggregator infTo();

	public Aggregator supOrEqTo();

	public Aggregator infOrEqTo();

	public Aggregator in();

	public Connector eq(Column column);

	public Connector notEq(Column column);

	public Connector supTo(Column column);
	
	public Connector infTo(Column column);

	public Connector supOrEqTo(Column column);

	public Connector infOrEqTo(Column column);

	public Connector eq(String value);

	public Connector notEq(String value);

	public Connector eq(SqlParameter<?> parameter);

	public Connector notEq(SqlParameter<?> parameter);

	public Connector like(String value);

	public Connector notLike(String value);

	public Connector like(SqlParameter<?> parameter);

	public Connector notLike(SqlParameter<?> parameter);


	public Connector between(String lower, String upper);

	public Connector between(SqlParameter<?> lower, SqlParameter<?> upper);

	public Connector supTo(String value);

	public Connector infTo(String value);

	public Connector supOrEqTo(String value);

	public Connector infOrEqTo(String value);

	public Connector supTo(SqlParameter<?> parameter);

	public Connector infTo(SqlParameter<?> parameter);

	public Connector supOrEqTo(SqlParameter<?> parameter);

	public Connector infOrEqTo(SqlParameter<?> parameter);

	public Connector in(String... value);

	public Connector notIn(String... value);

	public Connector eq(Integer value);

	public Connector notEq(Integer value);

	public Connector between(Integer lower, Integer upper);

	public Connector supTo(Integer value);

	public Connector infTo(Integer value);

	public Connector supOrEqTo(Integer value);

	public Connector infOrEqTo(Integer value);

	public Connector in(Integer... value);

	public Connector notIn(Integer... value);

	public Connector eq(Date value);

	public Connector notEq(Date value);

	public Connector between(Date lower, Date upper);

	public Connector supTo(Date value);

	public Connector infTo(Date value);

	public Connector supOrEqTo(Date value);

	public Connector infOrEqTo(Date value);

	public Connector in(Date... value);

	public Connector notIn(Date... value);

	public Connector eq(Double value);

	public Connector notEq(Double value);

	public Connector between(Double lower, Double upper);

	public Connector supTo(Double value);

	public Connector infTo(Double value);

	public Connector supOrEqTo(Double value);

	public Connector infOrEqTo(Double value);

	public Connector in(Double... value);

	public Connector notIn(Double... value);

	public Connector isNull();

	public Connector isNotNull();

	public Connector eq(Query subquery);

	public Connector notEq(Query subquery);

	public Connector in(Query subquery);

	public Connector notIn(Query subquery);

	public Connector supTo(Query subquery);

	public Connector infTo(Query subquery);

	public Connector supOrEqTo(Query subquery);

	public Connector infOrEqTo(Query subquery);

	public Connector exists(Query subquery);

	public Connector notExists(Query subquery);

	default Comparator where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

	default Connector eq(TableDescriptor<?> descriptor) {
		return eq(descriptor.column());
	}

	default Connector notEq(TableDescriptor<?> descriptor) {
		return notEq(descriptor.column());
	}

	default Connector supTo(TableDescriptor<?> descriptor) {
		return supTo(descriptor.column());
	}

	default Connector infTo(TableDescriptor<?> descriptor) {
		return infTo(descriptor.column());
	}

	default Connector supOrEqTo(TableDescriptor<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	default Connector infOrEqTo(TableDescriptor<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

}
