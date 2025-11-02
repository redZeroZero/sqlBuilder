package org.in.media.res.sqlBuilder.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public class TableImpl<T extends TableDescriptor<T>> implements Table {

	private final Map<String, Column> cols = new LinkedHashMap<>();

	private String name;

	private String alias;

	private String schema;

    public TableImpl(T[] descriptor) {
        this(descriptor, null);
    }

    public TableImpl(T[] descriptor, String schema) {
		Objects.requireNonNull(descriptor, "descriptor");
		if (descriptor.length == 0) {
			throw new IllegalArgumentException("Table descriptor array must not be empty");
		}
		this.name = descriptor[0].getClass().getSimpleName();
		this.schema = schema;
		for (T v : descriptor) {
			if (isAliasDescriptor(v)) {
				this.alias = v.alias();
			} else {
				createColumn(v.value(), v.alias(), column -> {
					cols.put(v.value(), column);
					v.bindColumn(column);
				});
			}
		}
	}

    public TableImpl(String name, String alias, Consumer<Builder> columnConfigurer) {
        this.name = Objects.requireNonNull(name, "name");
        this.alias = alias;
        Builder builder = new Builder(this);
        columnConfigurer.accept(builder);
        builder.build();
    }

    public TableImpl(String name, String alias, String schema, Consumer<Builder> columnConfigurer) {
        this(name, alias, columnConfigurer);
        this.schema = schema;
    }

	private static <T extends TableDescriptor<T>> boolean isAliasDescriptor(T descriptor) {
		return "T_ALIAS".equals(descriptor.fieldName());
	}

	public Column[] getColumns() {
		return cols.values().toArray(new Column[cols.values().size()]);
	}

	public Column get(TableDescriptor<?> descriptor) {
		return this.get(descriptor.value());
	}

	public Column get(String name) {
		return cols.get(name);
	}

	public String getName() {
		return name;
	}

	public void includeSchema(String schema) {
		this.schema = schema;
	}

	public String getSchema() {
		return schema;
	}

	public String getAlias() {
		return alias;
	}

	public boolean hasAlias() {
		return getAlias() != null && !getAlias().isEmpty() && !getAlias().isBlank();
	}

	public boolean hasTableName() {
		String tableName = tableName();
		return tableName != null && !tableName.isBlank() && !tableName.isEmpty();
	}

	public String tableName() {
		return this.hasAlias() ? this.getAlias() : this.getName();
	}

	private void createColumn(String name, String alias, Consumer<Column> consumer) {
		Column column = ColumnImpl.builder().name(name).alias(alias).table(this).build();
		consumer.accept(column);
	}

    public static final class Builder {
		private final TableImpl<?> table;

		private Builder(TableImpl<?> table) {
			this.table = table;
		}

		public Builder column(String name) {
			return column(name, null);
		}

        public Builder column(String name, String alias) {
            table.createColumn(name, alias, column -> table.cols.put(name, column));
            return this;
        }

		public Builder column(ColumnRef descriptor) {
			table.createColumn(descriptor.value(), descriptor.alias(), column -> {
				table.cols.put(descriptor.value(), column);
				descriptor.bindColumn(column);
			});
			return this;
		}

		private void build() {
			// no-op, hook to finish builder
		}
	}

}
