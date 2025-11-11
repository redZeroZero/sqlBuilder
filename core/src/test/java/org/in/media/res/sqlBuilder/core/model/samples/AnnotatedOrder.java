package org.in.media.res.sqlBuilder.core.model.samples;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlTable;

@SqlTable(name = "Orders", alias = "O", schema = "OPS")
public final class AnnotatedOrder {

    private AnnotatedOrder() {
    }

    @SqlColumn(name = "ORDER_ID", javaType = Long.class)
    public static ColumnRef<Long> ORDER_ID = ColumnRef.of("ORDER_ID", Long.class);

    @SqlColumn(name = "CUSTOMER_ID", javaType = Long.class)
    public static ColumnRef<Long> CUSTOMER_ID = ColumnRef.of("CUSTOMER_ID", Long.class);
}
