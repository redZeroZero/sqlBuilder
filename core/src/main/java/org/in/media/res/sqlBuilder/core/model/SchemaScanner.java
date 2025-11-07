package org.in.media.res.sqlBuilder.core.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

public final class SchemaScanner {

	private static final Map<ScanKey, ScanResult> RESULT_CACHE = new ConcurrentHashMap<>();

	private SchemaScanner() {
	}

	public static List<Table> scan(String basePackage) {
		return scan(basePackage, Thread.currentThread().getContextClassLoader());
	}

	public static List<Table> scan(String basePackage, ClassLoader classLoader) {
		return scanResult(basePackage, classLoader).tables();
	}

	public static TableFacets facets(String basePackage) {
		return facets(basePackage, Thread.currentThread().getContextClassLoader());
	}

	public static TableFacets facets(String basePackage, ClassLoader classLoader) {
		return TableFacets.build(scanResult(basePackage, classLoader).annotatedTables());
	}

	public static void invalidate(String basePackage, ClassLoader classLoader) {
		RESULT_CACHE.remove(new ScanKey(basePackage, classLoader));
	}

	@SuppressWarnings({ "rawtypes" })
	private static Table tableFromEnum(Class<?> enumClass) {
		TableDescriptor[] descriptors = (TableDescriptor[]) enumClass.getEnumConstants();
		return new TableImpl<>(descriptors);
	}

	private static Table tableFromAnnotatedClass(Class<?> candidate) {
		SqlTable tableAnnotation = candidate.getAnnotation(SqlTable.class);
		String tableName = tableAnnotation.name().isBlank() ? candidate.getSimpleName() : tableAnnotation.name();
		String tableAlias = tableAnnotation.alias().isBlank() ? null : tableAnnotation.alias();
		String schema = tableAnnotation.schema().isBlank() ? null : tableAnnotation.schema();
		List<Field> columnFields = Arrays.stream(candidate.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(SqlColumn.class)).toList();
		return new TableImpl<>(tableName, tableAlias, schema, builder -> {
			for (Field field : columnFields) {
				if (!Modifier.isStatic(field.getModifiers())) {
					throw new IllegalStateException("Field " + field.getName() + " on " + candidate.getName()
							+ " must be static to describe a column");
				}
				SqlColumn columnAnnotation = field.getAnnotation(SqlColumn.class);
				String columnName = columnAnnotation.name().isBlank() ? field.getName() : columnAnnotation.name();
				String columnAlias = columnAnnotation.alias().isBlank() ? null : columnAnnotation.alias();
				Class<?> javaType = resolveColumnType(field, columnAnnotation.javaType());
				if (javaType == Object.class) {
					throw new IllegalStateException("Column '" + columnName + "' on " + candidate.getName()
						+ " must declare a javaType via ColumnRef generics or @SqlColumn(javaType=...)");
				}
				ColumnRef<?> descriptor = ColumnRef.of(columnName, columnAlias, javaType);
				builder.column(descriptor);
				bindFieldIfNecessary(candidate, field, descriptor);
			}
		});
	}

	private static void bindFieldIfNecessary(Class<?> candidate, Field field, ColumnRef<?> descriptor) {
		if (!ColumnRef.class.isAssignableFrom(field.getType())) {
			return;
		}
		if (!Modifier.isStatic(field.getModifiers())) {
			throw new IllegalStateException("Field " + field.getName() + " on " + candidate.getName()
					+ " must be static to receive ColumnRef binding");
		}
		field.setAccessible(true);
		try {
			field.set(null, descriptor);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Unable to bind column descriptor to field " + field.getName(), ex);
		}
	}

	private static List<Table> deduplicateByName(List<Table> tables) {
		java.util.LinkedHashMap<String, Table> unique = new java.util.LinkedHashMap<>();
		for (Table table : tables) {
			unique.putIfAbsent(table.getName(), table);
		}
		return new ArrayList<>(unique.values());
	}

	private static Class<?> resolveColumnType(Field field, Class<?> annotationType) {
		if (annotationType != null && annotationType != Object.class) {
			return annotationType;
		}
		if (ColumnRef.class.isAssignableFrom(field.getType())) {
			var genericType = field.getGenericType();
			if (genericType instanceof java.lang.reflect.ParameterizedType parameterized) {
				var args = parameterized.getActualTypeArguments();
				if (args.length == 1 && args[0] instanceof Class<?> clazz) {
					return clazz;
				}
			}
		}
		Class<?> fieldType = field.getType();
		if (fieldType.isPrimitive()) {
			return wrapPrimitive(fieldType);
		}
		return fieldType;
	}

	private static Class<?> wrapPrimitive(Class<?> primitive) {
		return switch (primitive.getName()) {
		case "int" -> Integer.class;
		case "long" -> Long.class;
		case "double" -> Double.class;
		case "float" -> Float.class;
		case "boolean" -> Boolean.class;
		case "byte" -> Byte.class;
		case "short" -> Short.class;
		case "char" -> Character.class;
		default -> primitive;
		};
	}

	private static ScanResult scanResult(String basePackage, ClassLoader classLoader) {
		ScanKey key = new ScanKey(basePackage, classLoader);
		return RESULT_CACHE.computeIfAbsent(key, k -> buildScanResult(basePackage, classLoader));
	}

	private static ScanResult buildScanResult(String basePackage, ClassLoader classLoader) {
		Objects.requireNonNull(basePackage, "basePackage");
		List<Table> tables = new ArrayList<>();
		Map<Class<?>, Table> annotated = new LinkedHashMap<>();
		for (Class<?> candidate : ClasspathScanner.findClasses(basePackage, classLoader)) {
			if (candidate.isEnum() && TableDescriptor.class.isAssignableFrom(candidate)) {
				tables.add(tableFromEnum(candidate));
			} else if (candidate.isAnnotationPresent(SqlTable.class)) {
				Table table = tableFromAnnotatedClass(candidate);
				tables.add(table);
				annotated.put(candidate, table);
			}
		}
		return new ScanResult(List.copyOf(deduplicateByName(tables)), List.copyOf(annotated.entrySet()));
	}

	private record ScanResult(List<Table> tables, List<Map.Entry<Class<?>, Table>> annotatedEntries) {

		Map<Class<?>, Table> annotatedTables() {
			Map<Class<?>, Table> map = new LinkedHashMap<>();
			for (Map.Entry<Class<?>, Table> entry : annotatedEntries) {
				map.put(entry.getKey(), entry.getValue());
			}
			return map;
		}
	}

	private record ScanKey(String basePackage, ClassLoader classLoader) {
	}
}
