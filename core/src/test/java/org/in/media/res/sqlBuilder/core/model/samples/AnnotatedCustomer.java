package org.in.media.res.sqlBuilder.core.model.samples;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "Customer", alias = "C", schema = "CRM")
public final class AnnotatedCustomer {

    private AnnotatedCustomer() {
    }

    @SqlColumn(name = "ID", alias = "id", javaType = Long.class)
    public static ColumnRef<Long> ID = ColumnRef.of("ID", Long.class);

    @SqlColumn(name = "FIRST_NAME", alias = "firstName", javaType = String.class)
    public static ColumnRef<String> FIRST_NAME;

    @SqlColumn(name = "LAST_NAME", javaType = String.class)
    public static ColumnRef<String> LAST_NAME = ColumnRef.of("LAST_NAME", String.class);
}
