package org.in.media.res.sqlBuilder.core.query.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class PostgresDialectTest {

    private final PostgresDialect dialect = new PostgresDialect();

    @Test
    void quotesIdentifiersWithDoubleQuotes() {
        assertThat(dialect.quoteIdent("table"))
                .isEqualTo("\"table\"");
    }

    @Test
    void rendersLimitOffsetClause() {
        var clause = dialect.renderLimitOffset(20L, 2L);
        assertThat(clause.sql()).isEqualTo(" LIMIT ? OFFSET ?");
        assertThat(clause.params()).containsExactly(20L, 2L);
    }

    @Test
    void rendersFunctionsLowercase() {
        String sql;
        try (DialectContext.Scope scope = DialectContext.scope(dialect)) {
            sql = dialect.renderFunction("sum", List.of("col"));
        }
        assertThat(sql).isEqualTo("SUM(col)");
    }
}
