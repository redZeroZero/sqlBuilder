package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import org.in.media.res.sqlBuilder.interfaces.query.ICondition;
import org.in.media.res.sqlBuilder.interfaces.query.IWhere;
import org.in.media.res.sqlBuilder.interfaces.query.IWhereTranspiler;

public class OracleWhereTranspilerImpl implements IWhereTranspiler {

    private static final String WHERE = " WHERE ";

    @Override
    public String transpile(IWhere where) {
        if (where.conditions().isEmpty()) {
            return "";
        }
        SqlBuilder builder = SqlBuilder.from(WHERE);
        where.conditions().forEach(condition -> builder.append(condition.transpile()));
        return builder.toString();
    }
}
