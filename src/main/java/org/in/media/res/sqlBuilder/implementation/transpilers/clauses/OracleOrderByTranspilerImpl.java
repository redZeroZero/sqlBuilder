package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.interfaces.query.IOrderBy;
import org.in.media.res.sqlBuilder.interfaces.query.IOrderBy.Ordering;
import org.in.media.res.sqlBuilder.interfaces.query.IOrderByTranspiler;

public class OracleOrderByTranspilerImpl implements IOrderByTranspiler {

    private static final String ORDER_BY = " ORDER BY ";
    private static final String SEP = ", ";

    @Override
    public String transpile(IOrderBy orderBy) {
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
