package org.in.media.res.sqlBuilder.core.query.dialect;

import org.in.media.res.sqlBuilder.api.query.Dialect;

public final class DialectContext {

    private static final ThreadLocal<Dialect> CURRENT = new ThreadLocal<>();

    private DialectContext() {
    }

    public static Scope scope(Dialect dialect) {
        Dialect previous = CURRENT.get();
        CURRENT.set(dialect);
        return () -> {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        };
    }

    public static Dialect current() {
        Dialect dialect = CURRENT.get();
        return dialect != null ? dialect : Dialects.defaultDialect();
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
