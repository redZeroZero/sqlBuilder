package org.in.media.res.sqlBuilder.core.query.transpiler.oracle;

import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.api.query.WhereTranspiler;

public class OracleWhereTranspilerImpl implements WhereTranspiler {

    private static final String WHERE = " WHERE ";

    @Override
    public String transpile(Where where) {
        if (where.conditions().isEmpty()) {
            return "";
        }
        SqlBuilder builder = SqlBuilder.from(WHERE);
        where.conditions().forEach(condition -> builder.append(condition.transpile()));
        return builder.toString();
    }
}
