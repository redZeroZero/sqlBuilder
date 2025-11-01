package org.in.media.res.sqlBuilder.interfaces.model;

public interface ITableDescriptor<T> {

	String value();

	String alias();

	String fieldName();

	void bindColumn(IColumn column);

	IColumn column();

}
