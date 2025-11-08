package org.in.media.res.sqlBuilder.api.query;

/**
 * Symbolic parameter handle that can be reused across compiled queries. Each parameter has a
 * stable name and (optionally) an associated Java type for validation/documentation purposes.
 */
public interface SqlParameter<T> {

    /**
     * Logical name used when binding values (e.g. {@code Map.of("minSalary", 80_000)}).
     */
    String name();

    /**
     * Declared Java type of the parameter. Implementations may return {@code Object.class} when
     * no specific type is required.
     */
    Class<T> type();
}
