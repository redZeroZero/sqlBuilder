package org.in.media.res.sqlBuilder.api.model;

public interface TableDescriptor<T> {

	String value();

	String alias();

	String fieldName();

	void bindColumn(Column column);

	Column column();

}
