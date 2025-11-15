package org.in.media.res.sqlBuilder.api.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reusable, parameterised query template. {@link #bind(Object...)} and {@link #bind(Map)} produce
 * executable {@link SqlAndParams} instances.
 */
public final class CompiledQuery {

    private final String sql;
    private final List<Placeholder> placeholders;

    public CompiledQuery(String sql, List<Placeholder> placeholders) {
        this.sql = Objects.requireNonNull(sql, "sql");
        this.placeholders = List.copyOf(Objects.requireNonNull(placeholders, "placeholders"));
    }

    public String sql() {
        return sql;
    }

    public List<Placeholder> placeholders() {
        return placeholders;
    }

    public SqlAndParams bind(Object... values) {
        Objects.requireNonNull(values, "values");
        List<SqlParameter<?>> duplicateNames = findDuplicateParameters();
        if (!duplicateNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Varargs binding disallowed when parameters repeat: " + duplicateNames.get(0).name());
        }
        List<Object> params = new ArrayList<>(placeholders.size());
        int index = 0;
        for (Placeholder placeholder : placeholders) {
            if (placeholder.parameter() == null) {
                params.add(placeholder.fixedValue());
                continue;
            }
            if (index >= values.length) {
                throw new IllegalArgumentException("Missing value for parameter '" + placeholder.parameter().name() + "'");
            }
            Object value = values[index++];
            validateBindingValue(placeholder.parameter(), value);
            params.add(value);
        }
        if (index != values.length) {
            throw new IllegalArgumentException("Too many positional values supplied. Expected " + index + ", got "
                    + values.length);
        }
        return new SqlAndParams(sql, params);
    }

    public SqlAndParams bind(Map<String, ?> values) {
        Objects.requireNonNull(values, "values");
        validateUnknownKeys(values);
        List<Object> params = new ArrayList<>(placeholders.size());
        for (Placeholder placeholder : placeholders) {
            if (placeholder.parameter() == null) {
                params.add(placeholder.fixedValue());
                continue;
            }
            String name = placeholder.parameter().name();
            if (!values.containsKey(name)) {
                throw new IllegalArgumentException("Missing value for parameter '" + name + "'");
            }
            Object value = values.get(name);
            validateBindingValue(placeholder.parameter(), value);
            params.add(value);
        }
        return new SqlAndParams(sql, params);
    }

    private static void validateBindingValue(SqlParameter<?> parameter, Object value) {
        // Null values are permitted so optional predicates can be toggled at bind-time.
    }

    private void validateUnknownKeys(Map<String, ?> values) {
        for (String key : values.keySet()) {
            boolean known = placeholders.stream()
                    .filter(ph -> ph.parameter() != null)
                    .anyMatch(ph -> ph.parameter().name().equals(key));
            if (!known) {
                throw new IllegalArgumentException("Unknown parameter '" + key + "'");
            }
        }
    }

    private List<SqlParameter<?>> findDuplicateParameters() {
        List<SqlParameter<?>> duplicates = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        java.util.Set<String> repeated = new java.util.HashSet<>();
        for (Placeholder placeholder : placeholders) {
            if (placeholder.parameter() == null) {
                continue;
            }
            String name = placeholder.parameter().name();
            if (!seen.add(name)) {
                repeated.add(name);
            }
        }
        for (Placeholder placeholder : placeholders) {
            if (placeholder.parameter() != null && repeated.contains(placeholder.parameter().name())) {
                duplicates.add(placeholder.parameter());
            }
        }
        return duplicates;
    }

    public static record Placeholder(SqlParameter<?> parameter, Object fixedValue) {
        public Placeholder {
            if (parameter == null && fixedValue == null) {
                throw new IllegalArgumentException("Placeholder must have either a parameter or a fixed value");
            }
        }
    }
}
