package org.in.media.res.sqlBuilder.core.query.factory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.in.media.res.sqlBuilder.api.query.ColumnTranspiler;
import org.in.media.res.sqlBuilder.api.query.ConditionTranspiler;
import org.in.media.res.sqlBuilder.api.query.FromTranspiler;
import org.in.media.res.sqlBuilder.api.query.GroupByTranspiler;
import org.in.media.res.sqlBuilder.api.query.HavingTranspiler;
import org.in.media.res.sqlBuilder.api.query.LimitTranspiler;
import org.in.media.res.sqlBuilder.api.query.OrderByTranspiler;
import org.in.media.res.sqlBuilder.api.query.SelectTranspiler;
import org.in.media.res.sqlBuilder.api.query.WhereTranspiler;

public final class TranspilerFactory {

    private static final String DEFAULT_PACKAGE = "org.in.media.res.sqlBuilder.core.query.transpiler.oracle.";

    private static final ConcurrentMap<Class<?>, Object> CACHE = new ConcurrentHashMap<>();

    private TranspilerFactory() {
    }

    public static ConditionTranspiler instanciateConditionTranspiler() {
        return resolve(ConditionTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleConditionTranspilerImpl", ConditionTranspiler.class));
    }

    public static ColumnTranspiler instanciateColumnTranspiler() {
        return resolve(ColumnTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleColumnTranspilerImpl", ColumnTranspiler.class));
    }

    public static SelectTranspiler instanciateSelectTranspiler() {
        return resolve(SelectTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleSelectTranspilerImpl", SelectTranspiler.class));
    }

    public static FromTranspiler instanciateFromTranspiler() {
        return resolve(FromTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleFromTranspilerImpl", FromTranspiler.class));
    }

    public static WhereTranspiler instanciateWhereTranspiler() {
        return resolve(WhereTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleWhereTranspilerImpl", WhereTranspiler.class));
    }

    public static GroupByTranspiler instanciateGroupByTranspiler() {
        return resolve(GroupByTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleGroupByTranspilerImpl", GroupByTranspiler.class));
    }

    public static OrderByTranspiler instanciateOrderByTranspiler() {
        return resolve(OrderByTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleOrderByTranspilerImpl", OrderByTranspiler.class));
    }

    public static HavingTranspiler instanciateHavingTranspiler() {
        return resolve(HavingTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleHavingTranspilerImpl", HavingTranspiler.class));
    }

    public static LimitTranspiler instanciateLimitTranspiler() {
        return resolve(LimitTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "OracleLimitTranspilerImpl", LimitTranspiler.class));
    }

    private static <T> T resolve(Class<T> type, Supplier<T> supplier) {
        return type.cast(CACHE.computeIfAbsent(type, key -> Objects.requireNonNull(supplier.get(), "Unable to instantiate "+key.getName())));
    }

    private static <T> T instantiate(String className, Class<T> type) {
        try {
            return Class.forName(className).asSubclass(type).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate " + className, e);
        }
    }
}
