package org.in.media.res.sqlBuilder.api.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.core.model.ColumnRef;

/**
 * Simple typed container for row values keyed by {@link ColumnRef}s.
 */
public final class TableRow {

	private final Map<ColumnRef<?>, Object> values;

	private TableRow(Map<ColumnRef<?>, Object> values) {
		this.values = Collections.unmodifiableMap(values);
	}

	public static Builder builder() {
		return new Builder();
	}

	public <T> T get(ColumnRef<T> column) {
		return column.type().cast(values.get(column));
	}

	public Map<ColumnRef<?>, Object> asMap() {
		return values;
	}

	public static final class Builder {
		private final Map<ColumnRef<?>, Object> values = new LinkedHashMap<>();

		public <T> Builder set(ColumnRef<T> column, T value) {
			Objects.requireNonNull(column, "column");
			if (value != null && !column.type().isInstance(value)) {
				throw new IllegalArgumentException("Value " + value + " is not compatible with column " + column.value());
			}
			values.put(column, value);
			return this;
		}

		public TableRow build() {
			return new TableRow(new LinkedHashMap<>(values));
		}
	}
}
