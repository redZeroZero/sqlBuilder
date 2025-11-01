package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import static org.in.media.res.sqlBuilder.constants.JoinOperator.ON;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;

import java.util.Map;

import org.in.media.res.sqlBuilder.implementation.From.Joiner;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.IFrom;
import org.in.media.res.sqlBuilder.interfaces.query.IFromTranspiler;

public class OracleFromTranspilerImpl implements IFromTranspiler {

    private static final String SEP = ", ";
    private static final String FROM = " FROM ";
    private static final String ALIAS_SEP = " ";

    @Override
    public String transpile(IFrom from) {
        if (from.joins().isEmpty()) {
            return "";
        }
        if (from.joins().values().stream().noneMatch(joiner -> joiner == null)) {
            throw new IllegalStateException("FROM clause requires at least one base table before joins");
        }

        SqlBuilder builder = SqlBuilder.from(FROM);
        boolean baseTableEncountered = false;

        for (Map.Entry<ITable, Joiner> entry : from.joins().entrySet()) {
            ITable table = entry.getKey();
            Joiner joiner = entry.getValue();
            if (joiner == null) {
                if (baseTableEncountered) {
                    builder.append(SEP);
                }
                appendTable(builder, table);
                baseTableEncountered = true;
            } else {
                builder.append(joiner.getOp().value());
                appendTable(builder, table);
                appendJoinCondition(builder, table, joiner);
            }
        }

        return builder.toString();
    }

    private void appendTable(SqlBuilder builder, ITable table) {
        builder.appendTable(table);
        if (table.hasAlias()) {
            builder.append(ALIAS_SEP).append(table.getAlias());
        }
    }

    private void appendJoinCondition(SqlBuilder builder, ITable table, Joiner joiner) {
        IColumn left = joiner.getCol1();
        IColumn right = joiner.getCol2();
        if (left == null || right == null) {
            throw new IllegalStateException(
                    "JOIN on table " + table.getName() + " must define both columns via on(column1, column2)");
        }
        builder.append(ON.value()).appendColumn(left).append(EQ.value()).appendColumn(right);
    }
}
