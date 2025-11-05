package org.in.media.res.sqlBuilder.core.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

public final class SchemaScanner {

	private SchemaScanner() {
	}

	public static List<Table> scan(String basePackage) {
		return scan(basePackage, Thread.currentThread().getContextClassLoader());
	}

	public static List<Table> scan(String basePackage, ClassLoader classLoader) {
		Objects.requireNonNull(basePackage, "basePackage");
		List<Table> tables = new ArrayList<>();
		for (Class<?> candidate : ClasspathScanner.findClasses(basePackage, classLoader)) {
			if (candidate.isEnum() && TableDescriptor.class.isAssignableFrom(candidate)) {
				tables.add(tableFromEnum(candidate));
			} else if (candidate.isAnnotationPresent(SqlTable.class)) {
				tables.add(tableFromAnnotatedClass(candidate));
			}
		}
		return deduplicateByName(tables);
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
				SqlColumn columnAnnotation = field.getAnnotation(SqlColumn.class);
				String columnName = columnAnnotation.name().isBlank() ? field.getName() : columnAnnotation.name();
				String columnAlias = columnAnnotation.alias().isBlank() ? null : columnAnnotation.alias();
				ColumnRef<?> descriptor = ColumnRef.of(columnName, columnAlias);
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
}
