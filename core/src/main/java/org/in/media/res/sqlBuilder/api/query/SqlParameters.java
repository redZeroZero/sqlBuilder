package org.in.media.res.sqlBuilder.api.query;

import java.util.Objects;

/**
 * Factory helpers for {@link SqlParameter} instances.
 */
public final class SqlParameters {

    private SqlParameters() {
    }

    public static <T> SqlParameter<T> param(String name) {
        return param(name, cast(Object.class));
    }

    public static <T> SqlParameter<T> param(String name, Class<T> type) {
        return new DefaultSqlParameter<>(Objects.requireNonNull(name, "name").trim(),
                Objects.requireNonNull(type, "type"));
    }

    private static final class DefaultSqlParameter<T> implements SqlParameter<T> {
        private final String name;
        private final Class<T> type;

        private DefaultSqlParameter(String name, Class<T> type) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Parameter name must not be blank");
            }
            this.name = name;
            this.type = type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Class<T> type() {
            return type;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> cast(Class<?> type) {
        return (Class<T>) type;
    }
}
