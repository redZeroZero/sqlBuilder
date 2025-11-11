package org.in.media.res.sqlBuilder.core.query;

import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.spi.From;
import org.in.media.res.sqlBuilder.api.query.spi.GroupBy;
import org.in.media.res.sqlBuilder.api.query.spi.Having;
import org.in.media.res.sqlBuilder.api.query.spi.Limit;
import org.in.media.res.sqlBuilder.api.query.spi.OrderBy;
import org.in.media.res.sqlBuilder.api.query.spi.Select;
import org.in.media.res.sqlBuilder.api.query.spi.Where;

final class ClauseFactory {

    private ClauseFactory() {
    }

    public static Select instantiateSelect(Dialect dialect) {
        return new SelectImpl();
    }

    public static From instantiateFrom(Dialect dialect) {
        return new FromImpl();
    }

    public static Where instantiateWhere(Dialect dialect) {
        return new WhereImpl(dialect);
    }

    public static GroupBy instantiateGroupBy(Dialect dialect) {
        return new GroupByImpl();
    }

    public static OrderBy instantiateOrderBy(Dialect dialect) {
        return new OrderByImpl();
    }

    public static Having instantiateHaving(Dialect dialect) {
        return new HavingImpl(dialect);
    }

    public static Limit instantiateLimit(Dialect dialect) {
        return new LimitImpl();
    }
}
