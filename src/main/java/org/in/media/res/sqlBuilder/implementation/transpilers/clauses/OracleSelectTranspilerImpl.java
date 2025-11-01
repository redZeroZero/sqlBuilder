package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import java.util.Iterator;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.query.ISelect;
import org.in.media.res.sqlBuilder.interfaces.query.ISelectTranspiler;

public class OracleSelectTranspilerImpl implements ISelectTranspiler {

    private static final String SELECT_KEYWORD = "SELECT ";
    private static final String COLUMN_SEP = ", ";

    @Override
    public String transpile(ISelect select) {
        SqlBuilder builder = SqlBuilder.from(SELECT_KEYWORD);

        Iterator<Map.Entry<IColumn, AggregateOperator>> aggregates = select.aggColumns().entrySet().iterator();
        while (aggregates.hasNext()) {
            Map.Entry<IColumn, AggregateOperator> entry = aggregates.next();
            builder.append(entry.getValue().toString()).append('(').appendColumn(entry.getKey()).append(')');
            if (aggregates.hasNext() || !select.columns().isEmpty()) {
                builder.append(COLUMN_SEP);
            }
        }

        builder.join(select.columns(), COLUMN_SEP, column -> builder.append(column.transpile()));

        return builder.toString();
    }
}
