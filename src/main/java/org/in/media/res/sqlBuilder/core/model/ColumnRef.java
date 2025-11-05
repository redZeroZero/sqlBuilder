package org.in.media.res.sqlBuilder.core.model;

import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public final class ColumnRef<T> implements TableDescriptor<ColumnRef<T>> {

	private final String name;
	private final String alias;
	private final Class<T> javaType;
	private Column column;

	private ColumnRef(String name, String alias, Class<T> javaType) {
		this.name = Objects.requireNonNull(name, "name");
		this.alias = alias;
		this.javaType = javaType == null ? cast(Object.class) : javaType;
	}

	public static ColumnRef<Object> of(String name) {
		return new ColumnRef<>(name, null, Object.class);
	}

	public static ColumnRef<Object> of(String name, String alias) {
		return new ColumnRef<>(name, alias, Object.class);
	}

	public static <T> ColumnRef<T> of(String name, Class<T> javaType) {
		return new ColumnRef<>(name, null, Objects.requireNonNull(javaType, "javaType"));
	}

	public static <T> ColumnRef<T> of(String name, String alias, Class<T> javaType) {
		return new ColumnRef<>(name, alias, Objects.requireNonNull(javaType, "javaType"));
	}

	@Override
	public String value() {
		return name;
	}

	@Override
	public String alias() {
		return alias;
	}

	@Override
	public String fieldName() {
		return name;
	}

	@Override
	public void bindColumn(Column column) {
		this.column = column;
	}

	public Class<T> type() {
		return javaType;
	}

	@Override
	public Column column() {
		if (column == null) {
			throw new IllegalStateException("Column not bound for descriptor '" + name + "'");
		}
		return column;
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> cast(Class<?> clazz) {
		return (Class<T>) clazz;
	}
}
