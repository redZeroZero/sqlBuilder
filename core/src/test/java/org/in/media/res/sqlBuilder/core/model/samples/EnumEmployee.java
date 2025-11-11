package org.in.media.res.sqlBuilder.core.model.samples;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

/**
 * Enum-driven table descriptor to exercise SchemaScanner's enum path.
 */
public enum EnumEmployee implements TableDescriptor<EnumEmployee> {
    T_ALIAS("ignored", "EE"),
    ID("ID"),
    FIRST_NAME("FIRST_NAME");

    private final String value;
    private final String alias;
    private Column column;

    EnumEmployee(String value) {
        this(value, null);
    }

    EnumEmployee(String value, String alias) {
        this.value = value;
        this.alias = alias;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String alias() {
        return alias;
    }

    @Override
    public String fieldName() {
        return name();
    }

    @Override
    public void bindColumn(Column column) {
        this.column = column;
    }

    @Override
    public Column column() {
        return column;
    }
}
