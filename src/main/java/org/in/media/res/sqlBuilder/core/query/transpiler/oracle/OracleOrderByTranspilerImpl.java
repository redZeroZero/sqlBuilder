package org.in.media.res.sqlBuilder.core.query.transpiler.oracle;

import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.OrderBy.Ordering;
import org.in.media.res.sqlBuilder.api.query.OrderByTranspiler;

public class OracleOrderByTranspilerImpl implements OrderByTranspiler {

    private static final String ORDER_BY = " ORDER BY ";
    private static final String SEP = ", ";

    @Override
    public String transpile(OrderBy orderBy) {
        if (orderBy.orderings().isEmpty()) {
            return "";
        }
        SqlBuilder builder = SqlBuilder.from(ORDER_BY);
        builder.join(orderBy.orderings(), SEP, item -> {
            Ordering ordering = (Ordering) item;
            builder.appendColumn(ordering.column()).append(' ').append(ordering.direction().value());
        });
        return builder.toString();
    }
}
