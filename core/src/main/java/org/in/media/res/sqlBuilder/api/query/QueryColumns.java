package org.in.media.res.sqlBuilder.api.query;

import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.core.model.ScannedSchema;

/**
 * Convenience wrapper that bundles a table handle and its generated column view
 * so client code can reference both with a single variable.
 */
public final class QueryColumns<C> {

	private final Table table;
	private final C columns;

	private QueryColumns(Table table, C columns) {
		this.table = Objects.requireNonNull(table, "table");
		this.columns = Objects.requireNonNull(columns, "columns");
	}

	public Table table() {
		return table;
	}

	public C columns() {
		return columns;
	}

	public static <D, C> QueryColumns<C> of(ScannedSchema schema, Class<D> descriptorClass, Class<C> columnsType) {
		Objects.requireNonNull(schema, "schema");
		Objects.requireNonNull(descriptorClass, "descriptorClass");
		Objects.requireNonNull(columnsType, "columnsType");
		Table table = Objects.requireNonNull(schema.getTableBy(descriptorClass),
				() -> "No table registered for descriptor " + descriptorClass.getName());
		C columns = schema.facets().columns(descriptorClass, columnsType);
		return new QueryColumns<>(table, columns);
	}

	public static <C> QueryColumns<C> of(ScannedSchema schema, Class<C> columnsType) {
		return of(schema, resolveDescriptorClass(columnsType), columnsType);
	}

	private static <C> Class<?> resolveDescriptorClass(Class<C> columnsType) {
		String simpleName = columnsType.getSimpleName();
		if (!simpleName.endsWith("Columns")) {
			throw new IllegalArgumentException(
					"Cannot derive descriptor class from " + columnsType.getName() + " (missing 'Columns' suffix)");
		}
		String descriptorName = simpleName.substring(0, simpleName.length() - "Columns".length());
		String packageName = columnsType.getPackageName();
		String candidate = packageName.isBlank() ? descriptorName : packageName + "." + descriptorName;
		try {
			return Class.forName(candidate, true, columnsType.getClassLoader());
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException(
					"Unable to load descriptor class '" + candidate + "' for " + columnsType.getName(), ex);
		}
	}
}
