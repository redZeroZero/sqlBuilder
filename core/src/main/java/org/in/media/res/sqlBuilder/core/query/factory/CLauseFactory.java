package org.in.media.res.sqlBuilder.core.query.factory;

import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.Limit;
import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.Where;
import org.in.media.res.sqlBuilder.core.query.FromImpl;
import org.in.media.res.sqlBuilder.core.query.GroupByImpl;
import org.in.media.res.sqlBuilder.core.query.HavingImpl;
import org.in.media.res.sqlBuilder.core.query.LimitImpl;
import org.in.media.res.sqlBuilder.core.query.OrderByImpl;
import org.in.media.res.sqlBuilder.core.query.SelectImpl;
import org.in.media.res.sqlBuilder.core.query.WhereImpl;

public final class CLauseFactory {

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
