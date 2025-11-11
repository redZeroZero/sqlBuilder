package org.in.media.res.sqlBuilder.api.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;

/**
 * Runtime-generated typed view over annotated table descriptors.
 */
public final class TableFacets {

	private static final Map<Class<?>, Constructor<?>> COLUMN_VIEW_CONSTRUCTORS = new java.util.concurrent.ConcurrentHashMap<>();

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
		I generated = instantiateGeneratedColumns(viewType, facet);
		if (generated != null) {
			return generated;
		}
		return viewType.cast(ColumnViewProxy.create(facet, viewType));
	}

	public Map<Class<?>, Facet> all() {
		return facets;
	}

	public static TableFacets build(Map<Class<?>, Table> tableByDescriptor) {
		Map<Class<?>, Facet> mapping = new LinkedHashMap<>();
		tableByDescriptor
				.forEach((type, table) -> mapping.put(type, new Facet(table, collectColumns(type, table))));
		return new TableFacets(mapping);
	}

	private static Map<String, ColumnRef<?>> collectColumns(Class<?> descriptorClass, Table table) {
		Map<String, ColumnRef<?>> columns = new LinkedHashMap<>();
		for (Field field : descriptorClass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(SqlColumn.class)) {
				continue;
			}
			SqlColumn annotation = field.getAnnotation(SqlColumn.class);
			ColumnRef<?> descriptor = resolveDescriptor(field, annotation, table);
			String fieldName = field.getName();
			registerKey(columns, fieldName, descriptor);
			registerKey(columns, fieldName.toUpperCase(Locale.ROOT), descriptor);
			String columnName = annotation.name().isBlank() ? fieldName : annotation.name();
			registerKey(columns, columnName, descriptor);
			registerKey(columns, columnName.toUpperCase(Locale.ROOT), descriptor);
			if (!annotation.alias().isBlank()) {
				registerKey(columns, annotation.alias(), descriptor);
				registerKey(columns, annotation.alias().toUpperCase(Locale.ROOT), descriptor);
			}
		}
		return columns;
	}

	private static void registerKey(Map<String, ColumnRef<?>> columns, String key, ColumnRef<?> descriptor) {
		if (key == null || key.isBlank()) {
			return;
		}
		columns.putIfAbsent(key, descriptor);
	}

	private static ColumnRef<?> resolveDescriptor(Field field, SqlColumn annotation, Table table) {
		if (ColumnRef.class.isAssignableFrom(field.getType())) {
			field.setAccessible(true);
			try {
				ColumnRef<?> descriptor = (ColumnRef<?>) field.get(null);
				if (descriptor != null) {
					return descriptor;
				}
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to read column descriptor " + field.getName(), e);
			}
		}
		String columnName = annotation.name().isBlank() ? field.getName() : annotation.name();
		String alias = annotation.alias().isBlank() ? null : annotation.alias();
		Class<?> javaType = resolveJavaType(field, annotation);
		ColumnRef<?> descriptor = ColumnRef.of(columnName, alias, javaType);
		Column column = table.get(columnName);
		if (column == null) {
			throw new IllegalStateException("No column named '" + columnName + "' on table " + table.getName());
		}
		descriptor.bindColumn(column);
		if (ColumnRef.class.isAssignableFrom(field.getType())) {
			try {
				field.setAccessible(true);
				field.set(null, descriptor);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to bind column descriptor " + field.getName(), e);
			}
		}
		return descriptor;
	}

	private static Class<?> resolveJavaType(Field field, SqlColumn annotation) {
		if (annotation.javaType() != Object.class) {
			return annotation.javaType();
		}
		Class<?> type = field.getType();
		if (type.isPrimitive()) {
			return switch (type.getName()) {
			case "int" -> Integer.class;
			case "long" -> Long.class;
			case "double" -> Double.class;
			case "float" -> Float.class;
			case "boolean" -> Boolean.class;
			case "byte" -> Byte.class;
			case "short" -> Short.class;
			case "char" -> Character.class;
			default -> type;
			};
		}
		return type;
	}

	@SuppressWarnings("unchecked")
	private static <I> I instantiateGeneratedColumns(Class<I> viewType, Facet facet) {
		Constructor<?> constructor = COLUMN_VIEW_CONSTRUCTORS.computeIfAbsent(viewType,
				TableFacets::findConstructor);
		if (constructor == null) {
			return null;
		}
		try {
			return (I) constructor.newInstance(facet);
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("Unable to instantiate generated column view for " + viewType.getName(),
					ex);
		}
	}

	private static Constructor<?> findConstructor(Class<?> viewType) {
		String implName = viewType.getName() + "Impl";
		try {
			Class<?> implType = Class.forName(implName, true, viewType.getClassLoader());
			if (!viewType.isAssignableFrom(implType)) {
				return null;
			}
			Constructor<?> ctor = implType.getDeclaredConstructor(TableFacets.Facet.class);
			if (!Modifier.isPublic(ctor.getModifiers())) {
				ctor.setAccessible(true);
			}
			return ctor;
		} catch (ClassNotFoundException ex) {
			return null;
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("Invalid generated column view for " + viewType.getName(), ex);
		}
	}

	public record Facet(Table table, Map<String, ColumnRef<?>> columns) {
		public ColumnRef<?> column(String fieldName) {
			ColumnRef<?> descriptor = columns.get(fieldName);
			if (descriptor == null) {
				descriptor = columns.get(fieldName.toUpperCase(Locale.ROOT));
			}
			return Objects.requireNonNull(descriptor, () -> "No column bound for field '" + fieldName + "'");
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
			ColumnRef<?> ref = cache.computeIfAbsent(fieldName, facet::column);
			return ref;
		}
	}
}
