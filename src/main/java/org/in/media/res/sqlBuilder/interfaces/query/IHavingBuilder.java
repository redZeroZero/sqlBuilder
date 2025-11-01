package org.in.media.res.sqlBuilder.interfaces.query;

import java.util.Date;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITableDescriptor;

public interface IHavingBuilder {

	IHaving eq(String value);

	IHaving eq(Number value);

	IHaving eq(Date value);

	IHaving in(String... values);

	IHaving in(Number... values);

	IHaving supTo(Number value);

	IHaving supOrEqTo(Number value);

	IHaving infTo(Number value);

	IHaving infOrEqTo(Number value);

	IHaving supTo(IColumn column);

	default IHaving supTo(ITableDescriptor<?> descriptor) {
		return supTo(descriptor.column());
	}

	IHaving supOrEqTo(IColumn column);

	default IHaving supOrEqTo(ITableDescriptor<?> descriptor) {
		return supOrEqTo(descriptor.column());
	}

	IHaving infTo(IColumn column);

	default IHaving infTo(ITableDescriptor<?> descriptor) {
		return infTo(descriptor.column());
	}

	IHaving infOrEqTo(IColumn column);

	default IHaving infOrEqTo(ITableDescriptor<?> descriptor) {
		return infOrEqTo(descriptor.column());
	}

	IHavingBuilder and(IColumn column);

	default IHavingBuilder and(ITableDescriptor<?> descriptor) {
		return and(descriptor.column());
	}

	IHavingBuilder or(IColumn column);

	default IHavingBuilder or(ITableDescriptor<?> descriptor) {
		return or(descriptor.column());
	}

	IHavingBuilder min(IColumn column);

	default IHavingBuilder min(ITableDescriptor<?> descriptor) {
		return min(descriptor.column());
	}

	IHavingBuilder max(IColumn column);

	default IHavingBuilder max(ITableDescriptor<?> descriptor) {
		return max(descriptor.column());
	}

	IHavingBuilder sum(IColumn column);

	default IHavingBuilder sum(ITableDescriptor<?> descriptor) {
		return sum(descriptor.column());
	}

	IHavingBuilder avg(IColumn column);

	default IHavingBuilder avg(ITableDescriptor<?> descriptor) {
		return avg(descriptor.column());
	}

	IHavingBuilder col(IColumn column);

	default IHavingBuilder col(ITableDescriptor<?> descriptor) {
		return col(descriptor.column());
	}
}
