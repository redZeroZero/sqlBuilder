package org.in.media.res.sqlBuilder.core.query.transpiler.oracle;

import java.util.Iterator;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.SelectTranspiler;

public class OracleSelectTranspilerImpl implements SelectTranspiler {

    private static final String SELECT_KEYWORD = "SELECT ";
    private static final String COLUMN_SEP = ", ";

    @Override
    public String transpile(Select select) {
        String keyword = select.isDistinct() ? "SELECT DISTINCT " : SELECT_KEYWORD;
        SqlBuilder builder = SqlBuilder.from(keyword);

        Iterator<Map.Entry<Column, AggregateOperator>> aggregates = select.aggColumns().entrySet().iterator();
        while (aggregates.hasNext()) {
            Map.Entry<Column, AggregateOperator> entry = aggregates.next();
            builder.append(entry.getValue().toString()).append('(').appendColumn(entry.getKey()).append(')');
            if (aggregates.hasNext() || !select.columns().isEmpty()) {
                builder.append(COLUMN_SEP);
            }
        }

        builder.join(select.columns(), COLUMN_SEP, column -> builder.append(column.transpile()));

        return builder.toString();
    }
}
