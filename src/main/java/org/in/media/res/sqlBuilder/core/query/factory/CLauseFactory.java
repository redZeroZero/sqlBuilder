package org.in.media.res.sqlBuilder.core.query.factory;

import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.GroupBy;
import org.in.media.res.sqlBuilder.api.query.Having;
import org.in.media.res.sqlBuilder.api.query.Limit;
import org.in.media.res.sqlBuilder.api.query.OrderBy;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.Where;

public final class CLauseFactory {

    private static final String DEFAULT_PACKAGE = "org.in.media.res.sqlBuilder.core.query.";

    private CLauseFactory() {
    }

    public static Select instanciateSelect() {
        return instantiate(DEFAULT_PACKAGE + "SelectImpl", Select.class);
    }

    public static From instanciateFrom() {
        return instantiate(DEFAULT_PACKAGE + "FromImpl", From.class);
    }

    public static Where instanciateWhere() {
        return instantiate(DEFAULT_PACKAGE + "WhereImpl", Where.class);
    }

    public static GroupBy instanciateGroupBy() {
        return instantiate(DEFAULT_PACKAGE + "GroupByImpl", GroupBy.class);
    }

    public static OrderBy instanciateOrderBy() {
        return instantiate(DEFAULT_PACKAGE + "OrderByImpl", OrderBy.class);
    }

    public static Having instanciateHaving() {
        return instantiate(DEFAULT_PACKAGE + "HavingImpl", Having.class);
    }

    public static Limit instanciateLimit() {
        return instantiate(DEFAULT_PACKAGE + "LimitImpl", Limit.class);
    }

    private static <T> T instantiate(String className, Class<T> type) {
        try {
            return Class.forName(className).asSubclass(type).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate " + className, e);
        }
    }
}
