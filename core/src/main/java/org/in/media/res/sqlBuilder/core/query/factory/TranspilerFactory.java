package org.in.media.res.sqlBuilder.core.query.factory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import org.in.media.res.sqlBuilder.api.query.spi.ColumnTranspiler;
import org.in.media.res.sqlBuilder.api.query.spi.ConditionTranspiler;
import org.in.media.res.sqlBuilder.api.query.spi.FromTranspiler;
import org.in.media.res.sqlBuilder.api.query.spi.GroupByTranspiler;
import org.in.media.res.sqlBuilder.api.query.spi.HavingTranspiler;
import org.in.media.res.sqlBuilder.api.query.spi.LimitTranspiler;
import org.in.media.res.sqlBuilder.api.query.spi.OrderByTranspiler;
import org.in.media.res.sqlBuilder.api.query.spi.SelectTranspiler;
import org.in.media.res.sqlBuilder.api.query.spi.WhereTranspiler;

public final class TranspilerFactory {

    private static final String DEFAULT_PACKAGE = "org.in.media.res.sqlBuilder.core.query.transpiler.defaults.";

    private static final ConcurrentMap<Class<?>, Object> CACHE = new ConcurrentHashMap<>();

    private TranspilerFactory() {
    }

	public static ConditionTranspiler instantiateConditionTranspiler() {
        return resolve(ConditionTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultConditionTranspiler", ConditionTranspiler.class));
    }

	public static ColumnTranspiler instantiateColumnTranspiler() {
        return resolve(ColumnTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultColumnTranspiler", ColumnTranspiler.class));
    }

	public static SelectTranspiler instantiateSelectTranspiler() {
        return resolve(SelectTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultSelectTranspiler", SelectTranspiler.class));
    }

	public static FromTranspiler instantiateFromTranspiler() {
        return resolve(FromTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultFromTranspiler", FromTranspiler.class));
    }

	public static WhereTranspiler instantiateWhereTranspiler() {
        return resolve(WhereTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultWhereTranspiler", WhereTranspiler.class));
    }

	public static GroupByTranspiler instantiateGroupByTranspiler() {
        return resolve(GroupByTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultGroupByTranspiler", GroupByTranspiler.class));
    }

	public static OrderByTranspiler instantiateOrderByTranspiler() {
        return resolve(OrderByTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultOrderByTranspiler", OrderByTranspiler.class));
    }

	public static HavingTranspiler instantiateHavingTranspiler() {
        return resolve(HavingTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultHavingTranspiler", HavingTranspiler.class));
    }

	public static LimitTranspiler instantiateLimitTranspiler() {
        return resolve(LimitTranspiler.class, () -> instantiate(DEFAULT_PACKAGE + "DefaultLimitTranspiler", LimitTranspiler.class));
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
