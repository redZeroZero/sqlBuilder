package org.in.media.res.sqlBuilder.core.query;

import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.Limit;
import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.Where;
final class CLauseFactory {

    private CLauseFactory() {
    }

    public static Select instanciateSelect(Dialect dialect) {
        return new SelectImpl();
    }

    public static From instanciateFrom(Dialect dialect) {
        return new FromImpl();
    }

    public static Where instanciateWhere(Dialect dialect) {
        return new WhereImpl(dialect);
    }

    public static GroupBy instanciateGroupBy(Dialect dialect) {
        return new GroupByImpl();
    }

    public static OrderBy instanciateOrderBy(Dialect dialect) {
        return new OrderByImpl();
    }

    public static Having instanciateHaving(Dialect dialect) {
        return new HavingImpl(dialect);
    }

    public static Limit instanciateLimit(Dialect dialect) {
        return new LimitImpl();
    }
}
