package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IComparator {

	public IComparator condition(ICondition condition);

	public IComparator where(IColumn column);

	default IComparator where(ITableDescriptor<?> descriptor) {
		return where(descriptor.column());
	}

	public IAggregator eq();

	public IAggregator supTo();

	public IAggregator infTo();

	public IAggregator supOrEqTo();

	public IAggregator infOrEqTo();

	public IAggregator in();

	public IConnector eq(IColumn column);

	default IConnector eq(ITableDescriptor<?> descriptor) {
		return eq(descriptor.column());
	}

	public IConnector supTo(IColumn column);

	default IConnector supTo(ITableDescriptor<?> descriptor) {
		return supTo(descriptor.column());
	}

	public IConnector infTo(IColumn column);

	default IConnector infTo(ITableDescriptor<?> descriptor) {
		return infTo(descriptor.column());
	}

	public IConnector supOrEqTo(IColumn column);

	default IConnector supOrEqTo(ITableDescriptor<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	public IConnector infOrEqTo(IColumn column);

	default IConnector infOrEqTo(ITableDescriptor<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	public IConnector eq(String value);

	public IConnector supTo(String value);

	public IConnector infTo(String value);

	public IConnector supOrEqTo(String value);

	public IConnector infOrEqTo(String value);

	public IConnector in(String... value);

	public IConnector eq(Integer value);

	public IConnector supTo(Integer value);

	public IConnector infTo(Integer value);

	public IConnector supOrEqTo(Integer value);

	public IConnector infOrEqTo(Integer value);

	public IConnector in(Integer... value);

	public IConnector eq(Date value);

	public IConnector supTo(Date value);

	public IConnector infTo(Date value);

	public IConnector supOrEqTo(Date value);

	public IConnector infOrEqTo(Date value);

	public IConnector in(Date... value);

	public IConnector eq(Double value);

	public IConnector supTo(Double value);

	public IConnector infTo(Double value);

	public IConnector supOrEqTo(Double value);

	public IConnector infOrEqTo(Double value);

	public IConnector in(Double... value);
}
