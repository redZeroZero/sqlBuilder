package org.in.media.res.sqlBuilder.core.query.transpiler.defaults;

import java.util.Iterator;
import java.util.Map;

import java.util.List;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.spi.Select;
import org.in.media.res.sqlBuilder.api.query.spi.SelectTranspiler;
import org.in.media.res.sqlBuilder.core.query.SelectProjectionSupport;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;

public class DefaultSelectTranspiler implements SelectTranspiler {

    private static final String SELECT_KEYWORD = "SELECT ";
    private static final String COLUMN_SEP = ", ";

	@Override
	public String transpile(Select select) {
		String keyword = select.isDistinct() ? "SELECT DISTINCT " : SELECT_KEYWORD;
		DefaultSqlBuilder builder = DefaultSqlBuilder.from(keyword);
		List<String> hints = select.hints();
		if (!hints.isEmpty()) {
			builder.append(String.join(" ", hints)).append(' ');
		}

		if (select instanceof SelectProjectionSupport support) {
			renderEntries(builder, support.projections());
			return builder.toString();
		}

		Iterator<Map.Entry<Column, AggregateOperator>> aggregates = select.aggColumns().entrySet().iterator();
		while (aggregates.hasNext()) {
			Map.Entry<Column, AggregateOperator> entry = aggregates.next();
			Column column = entry.getKey();
			String columnSql = column.transpile(false);
			String rendered = DialectContext.current().renderFunction(entry.getValue().logicalName(), List.of(columnSql));
			builder.append(rendered);
			if (aggregates.hasNext() || !select.columns().isEmpty()) {
				builder.append(COLUMN_SEP);
			}
		}

		builder.join(select.columns(), COLUMN_SEP, column -> builder.append(column.transpile()));
		return builder.toString();
	}

	private void renderEntries(DefaultSqlBuilder builder, List<SelectProjectionSupport.SelectProjection> entries) {
		boolean first = true;
		for (SelectProjectionSupport.SelectProjection entry : entries) {
			if (!first) {
				builder.append(COLUMN_SEP);
			}
			switch (entry.type()) {
			case COLUMN -> {
				if (entry.column() != null) {
					builder.append(entry.column().transpile());
				}
			}
			case AGGREGATE -> {
				if (entry.column() != null) {
					String columnSql = entry.column().transpile(false);
					String rendered = DialectContext.current().renderFunction(entry.aggregate().logicalName(),
							List.of(columnSql));
					builder.append(rendered);
				}
			}
			case RAW -> {
				if (entry.fragment() != null) {
					builder.append(entry.fragment().sql());
				}
			}
			}
			first = false;
		}
	}
}
