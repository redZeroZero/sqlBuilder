package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import static org.in.media.res.sqlBuilder.constants.JoinOperator.ON;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;

import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.spi.From;
import org.in.media.res.sqlBuilder.api.query.spi.From.JoinSpec;
import org.in.media.res.sqlBuilder.api.query.spi.FromTranspiler;
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

        DefaultSqlBuilder builder = DefaultSqlBuilder.from(FROM);
		Map.Entry<Table, JoinSpec> base = from.joins().entrySet()
				.stream()
				.filter(entry -> entry.getValue() == null)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("FROM clause requires at least one base table before joins"));

		appendTable(builder, base.getKey());

		for (Map.Entry<Table, JoinSpec> entry : from.joins().entrySet()) {
			if (entry == base) {
				continue;
			}
			Table table = entry.getKey();
			JoinSpec joiner = entry.getValue();
			if (joiner == null) {
				builder.append(SEP);
				appendTable(builder, table);
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
