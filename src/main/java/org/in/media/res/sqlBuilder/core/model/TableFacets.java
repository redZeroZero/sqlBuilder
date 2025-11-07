package org.in.media.res.sqlBuilder.core.model;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableRow;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;

/**
 * Runtime-generated typed view over annotated table descriptors.
 */
public final class TableFacets {

	private final Map<Class<?>, Facet> facets;

	TableFacets(Map<Class<?>, Facet> facets) {
		this.facets = Collections.unmodifiableMap(facets);
	}

	public Facet facetFor(Class<?> descriptorClass) {
		return Objects.requireNonNull(facets.get(descriptorClass),
				() -> "No typed facet registered for " + descriptorClass.getName());
	}

	public <I> I columns(Class<?> descriptorClass, Class<I> viewType) {
		Facet facet = facetFor(descriptorClass);
		return viewType.cast(ColumnViewProxy.create(facet, viewType));
	}

	public Map<Class<?>, Facet> all() {
		return facets;
	}

	public static TableFacets build(Map<Class<?>, Table> tableByDescriptor) {
		Map<Class<?>, Facet> mapping = new LinkedHashMap<>();
		tableByDescriptor.forEach((type, table) -> mapping.put(type, new Facet(table, collectColumns(type))));
		return new TableFacets(mapping);
	}

	private static Map<String, ColumnRef<?>> collectColumns(Class<?> descriptorClass) {
		Map<String, ColumnRef<?>> columns = new LinkedHashMap<>();
		for (Field field : descriptorClass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(SqlColumn.class)) {
				continue;
			}
			if (!ColumnRef.class.isAssignableFrom(field.getType())) {
				continue;
			}
			field.setAccessible(true);
			try {
				ColumnRef<?> columnRef = (ColumnRef<?>) field.get(null);
				if (columnRef != null) {
					columns.put(field.getName(), columnRef);
				}
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to read column descriptor " + field.getName(), e);
			}
		}
		return columns;
	}

	public record Facet(Table table, Map<String, ColumnRef<?>> columns) {
		public ColumnRef<?> column(String fieldName) {
			return Objects.requireNonNull(columns.get(fieldName),
					() -> "No column bound for field '" + fieldName + "'");
		}

		public TableRow.Builder rowBuilder() {
			return TableRow.builder();
		}
	}

	private static final class ColumnViewProxy implements java.lang.reflect.InvocationHandler {

		private final Facet facet;
		private final Map<String, ColumnRef<?>> cache = new java.util.concurrent.ConcurrentHashMap<>();

		private ColumnViewProxy(Facet facet) {
			this.facet = facet;
		}

		static <I> I create(Facet facet, Class<I> viewType) {
			return viewType.cast(java.lang.reflect.Proxy.newProxyInstance(
					viewType.getClassLoader(), new Class<?>[] { viewType }, new ColumnViewProxy(facet)));
		}

		@Override
		public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
			if (method.getDeclaringClass() == Object.class) {
				return method.invoke(this, args);
			}
			if (method.getParameterCount() > 0) {
				throw new IllegalStateException("Column view methods must have no arguments: " + method.getName());
			}
			String fieldName = method.getName();
			ColumnRef<?> ref = cache.computeIfAbsent(fieldName, name -> facet.column(name.toUpperCase()));
			return ref;
		}
	}
}
