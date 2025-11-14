package org.in.media.res.sqlBuilder.spring.jdbc;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Thin bridge that runs sqlBuilder output on a Spring {@link JdbcTemplate}.
 */
public final class SqlBuilderJdbcTemplate {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SqlBuilderJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate, "jdbcTemplate");
        this.namedParameterJdbcTemplate = null;
    }

    public SqlBuilderJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = Objects.requireNonNull(namedParameterJdbcTemplate,
                "namedParameterJdbcTemplate");
        this.jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }

    public <T> List<T> query(SqlAndParams sqlAndParams, RowMapper<T> rowMapper) {
        Objects.requireNonNull(sqlAndParams, "sqlAndParams");
        Objects.requireNonNull(rowMapper, "rowMapper");
        return jdbcTemplate.query(sqlAndParams.sql(), toArray(sqlAndParams.params()), rowMapper);
    }

    public <T> T queryForObject(SqlAndParams sqlAndParams, RowMapper<T> rowMapper) {
        Objects.requireNonNull(sqlAndParams, "sqlAndParams");
        Objects.requireNonNull(rowMapper, "rowMapper");
        return jdbcTemplate.queryForObject(sqlAndParams.sql(), toArray(sqlAndParams.params()), rowMapper);
    }

    public int update(SqlAndParams sqlAndParams) {
        Objects.requireNonNull(sqlAndParams, "sqlAndParams");
        return jdbcTemplate.update(sqlAndParams.sql(), toArray(sqlAndParams.params()));
    }

    public int update(CompiledQuery compiledQuery, Map<String, ?> paramValues) {
        Objects.requireNonNull(paramValues, "paramValues");
        return update(compiledQuery.bind(paramValues));
    }

    public <T> List<T> query(CompiledQuery compiledQuery, Map<String, ?> paramValues, RowMapper<T> rowMapper) {
        Objects.requireNonNull(paramValues, "paramValues");
        return query(compiledQuery.bind(paramValues), rowMapper);
    }

    public <T> T queryForObject(CompiledQuery compiledQuery, Map<String, ?> paramValues, RowMapper<T> rowMapper) {
        Objects.requireNonNull(paramValues, "paramValues");
        return queryForObject(compiledQuery.bind(paramValues), rowMapper);
    }

    private static Object[] toArray(List<Object> params) {
        if (params.isEmpty()) {
            return new Object[0];
        }
        return params.toArray();
    }
}
