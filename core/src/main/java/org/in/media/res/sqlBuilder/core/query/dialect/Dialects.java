package org.in.media.res.sqlBuilder.core.query.dialect;

import org.in.media.res.sqlBuilder.api.query.Dialect;

public final class Dialects {

    private static final Dialect ORACLE = new OracleDialect();

    private Dialects() {
    }

    public static Dialect defaultDialect() {
        return ORACLE;
    }
}
