package org.in.media.res.sqlBuilder.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.core.model.TableImpl;

/**
 * Factory for programmatically defined tables. Use this when annotation-based
 * scanning is not available (fat jars, restricted classloaders, etc.).
 */
public final class Tables {

	private Tables() {
	}

	public static Builder builder(String name) {
		return builder(name, null);
	}

	public static Builder builder(String name, String alias) {
		return new Builder(name, alias);
	}

	public static final class Builder {

		private final String name;
		private final String alias;
		private final List<Consumer<TableImpl.Builder>> columnSteps = new ArrayList<>();
		private String schema;

		private Builder(String name, String alias) {
			this.name = Objects.requireNonNull(name, "name");
			this.alias = alias;
		}

		public Builder schema(String schema) {
			this.schema = schema;
			return this;
		}

		public Builder column(String columnName) {
			return column(columnName, null);
		}

		public Builder column(String columnName, String columnAlias) {
			Objects.requireNonNull(columnName, "columnName");
			columnSteps.add(builder -> builder.column(columnName, columnAlias));
			return this;
		}

		public Builder column(ColumnRef<?> descriptor) {
			Objects.requireNonNull(descriptor, "descriptor");
			columnSteps.add(builder -> builder.column(descriptor));
			return this;
		}

		public Table build() {
			if (columnSteps.isEmpty()) {
				throw new IllegalStateException("Manual table builder requires at least one column");
			}
			Consumer<TableImpl.Builder> configurer = builder -> columnSteps.forEach(step -> step.accept(builder));
			return schema == null
					? new TableImpl<>(name, alias, configurer)
					: new TableImpl<>(name, alias, schema, configurer);
		}
	}
}
