package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.OrderBy.Ordering;
import org.in.media.res.sqlBuilder.api.query.OrderByTranspiler;

public class DefaultOrderByTranspiler implements OrderByTranspiler {

    private static final String ORDER_BY = " ORDER BY ";
    private static final String SEP = ", ";

    @Override
    public String transpile(OrderBy orderBy) {
        if (orderBy.orderings().isEmpty()) {
            return "";
        }
        DefaultSqlBuilder builder = DefaultSqlBuilder.from(ORDER_BY);
        builder.join(orderBy.orderings(), SEP, item -> {
            Ordering ordering = (Ordering) item;
            builder.appendColumn(ordering.column()).append(' ').append(ordering.direction().value());
        });
        return builder.toString();
    }
}
