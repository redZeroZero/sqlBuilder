package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import static org.in.media.res.sqlBuilder.constants.JoinOperator.ON;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;

import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.From.JoinSpec;
import org.in.media.res.sqlBuilder.api.query.FromTranspiler;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.constants.JoinOperator;
import org.in.media.res.sqlBuilder.core.query.FromRawSupport;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;

public class DefaultFromTranspiler implements FromTranspiler {

    private static final String SEP = ", ";
    private static final String FROM = " FROM ";
    private static final String ALIAS_SEP = " ";

    @Override
    public String transpile(From from) {
		FromRawSupport rawSupport = from instanceof FromRawSupport support ? support : null;
		if (rawSupport != null && rawSupport.rawBaseFragment() != null) {
			DefaultSqlBuilder builder = DefaultSqlBuilder.from(FROM);
			builder.append(rawSupport.rawBaseFragment().sql());
			appendRawJoins(builder, rawSupport.rawJoinFragments());
			return builder.toString();
		}
        if (from.joins().isEmpty()) {
            return "";
        }
        if (from.joins().values().stream().noneMatch(joiner -> joiner == null)) {
            throw new IllegalStateException("FROM clause requires at least one base table before joins");
        }

        DefaultSqlBuilder builder = DefaultSqlBuilder.from(FROM);
        boolean baseTableEncountered = false;

        for (Map.Entry<Table, JoinSpec> entry : from.joins().entrySet()) {
            Table table = entry.getKey();
            JoinSpec joiner = entry.getValue();
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

		if (rawSupport != null) {
			appendRawJoins(builder, rawSupport.rawJoinFragments());
		}

        return builder.toString();
    }

	private void appendTable(DefaultSqlBuilder builder, Table table) {
		builder.appendTable(table);
		if (table.hasAlias()) {
			builder.append(ALIAS_SEP).append(DialectContext.current().quoteIdent(table.getAlias()));
		}
	}

	private void appendJoinCondition(DefaultSqlBuilder builder, Table table, JoinSpec joiner) {
		if (joiner.getOp() == JoinOperator.CROSS_JOIN) {
			return;
		}
		Column left = joiner.getCol1();
		Column right = joiner.getCol2();
		if (left == null || right == null) {
			throw new IllegalStateException(
					"JOIN on table " + table.getName() + " must define both columns via on(column1, column2)");
        }
        builder.append(ON.value()).appendColumn(left).append(EQ.value()).appendColumn(right);
    }

	private void appendRawJoins(DefaultSqlBuilder builder, List<FromRawSupport.RawJoinFragmentView> rawJoins) {
		for (FromRawSupport.RawJoinFragmentView fragment : rawJoins) {
			builder.append(' ').append(fragment.operator().value()).append(' ').append(fragment.fragment().sql());
		}
	}
}
