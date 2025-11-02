package org.in.media.res.sqlBuilder.api.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

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

	public Connector supTo(Column column);
	
	public Connector infTo(Column column);

	public Connector supOrEqTo(Column column);

	public Connector infOrEqTo(Column column);

	public Connector eq(String value);

	public Connector supTo(String value);

	public Connector infTo(String value);

	public Connector supOrEqTo(String value);

	public Connector infOrEqTo(String value);

	public Connector in(String... value);

	public Connector eq(Integer value);

	public Connector supTo(Integer value);

	public Connector infTo(Integer value);

	public Connector supOrEqTo(Integer value);

	public Connector infOrEqTo(Integer value);

	public Connector in(Integer... value);

	public Connector eq(Date value);

	public Connector supTo(Date value);

	public Connector infTo(Date value);

	public Connector supOrEqTo(Date value);

	public Connector infOrEqTo(Date value);

	public Connector in(Date... value);

	public Connector eq(Double value);

	public Connector supTo(Double value);

	public Connector infTo(Double value);

	public Connector supOrEqTo(Double value);

	public Connector infOrEqTo(Double value);

	public Connector in(Double... value);

	default Comparator where(TableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

	default Connector eq(TableDescriptor<?> descriptor) {
		return eq(descriptor.column());
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
