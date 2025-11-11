package org.in.media.res.sqlBuilder.core.query.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class OracleDialectTest {

    private final OracleDialect dialect = new OracleDialect();

    @Test
    void quotesIdentifiersWithDoubleQuotes() {
        assertThat(dialect.quoteIdent("Employee"))
                .isEqualTo("\"Employee\"");
    }

    @Test
    void rendersLimitUsingFetchSyntax() {
        var clause = dialect.renderLimitOffset(10L, 5L);
        assertThat(clause.sql()).isEqualTo(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        assertThat(clause.params()).containsExactly(5L, 10L);
    }

    @Test
    void rendersFunctionsUppercase() {
        String sql;
        try (DialectContext.Scope scope = DialectContext.scope(dialect)) {
            sql = dialect.renderFunction("sum", List.of("\"E\".\"SALARY\""));
        }
        assertThat(sql).isEqualTo("SUM(\"E\".\"SALARY\")");
    }
}
