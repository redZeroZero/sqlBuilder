package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.api.query.WhereTranspiler;

public class DefaultWhereTranspiler implements WhereTranspiler {

    private static final String WHERE = " WHERE ";

    @Override
    public String transpile(Where where) {
        if (where.conditions().isEmpty()) {
            return "";
        }
        DefaultSqlBuilder builder = DefaultSqlBuilder.from(WHERE);
        where.conditions().forEach(condition -> builder.append(condition.transpile()));
        return builder.toString();
    }
}
