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
import org.in.media.res.sqlBuilder.api.query.window.WindowExpression;
import org.in.media.res.sqlBuilder.api.query.window.WindowFunction;
import org.in.media.res.sqlBuilder.api.query.window.WindowFrame;
import org.in.media.res.sqlBuilder.api.query.window.WindowOrdering;

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
			case WINDOW -> {
				renderWindow(builder, entry.windowFunction());
			}
			}
			first = false;
		}
	}

	private void renderWindow(DefaultSqlBuilder builder, WindowFunction window) {
		if (window == null) {
			return;
		}
		String functionSql = DialectContext.current()
				.renderFunction(window.logicalName(), renderArgs(window.arguments()));
		builder.append(functionSql).append(" OVER(");
		boolean needsSpace = false;
		if (!window.partitions().isEmpty()) {
			builder.append("PARTITION BY ");
			builder.append(String.join(", ", renderExpressions(window.partitions())));
			needsSpace = true;
		}
		if (!window.orderings().isEmpty()) {
			if (needsSpace) {
				builder.append(' ');
			}
			builder.append("ORDER BY ");
			builder.append(String.join(", ", renderOrderings(window.orderings())));
			needsSpace = true;
		}
		if (window.frame() != null) {
			if (needsSpace) {
				builder.append(' ');
			}
			renderFrame(builder, window.frame());
		}
		builder.append(')');
		if (window.alias() != null && !window.alias().isBlank()) {
			builder.append(" AS ").append(DialectContext.current().quoteIdent(window.alias()));
		}
	}

	private List<String> renderArgs(List<WindowExpression> expressions) {
		return renderExpressions(expressions);
	}

	private List<String> renderExpressions(List<WindowExpression> expressions) {
		return expressions.stream().map(this::renderExpression).toList();
	}

	private String renderExpression(WindowExpression expression) {
		if (expression == null) {
			return "";
		}
		if (expression.isColumn()) {
			return expression.column().transpile(false);
		}
		if (expression.isRaw()) {
			return expression.rawFragment().sql();
		}
		return "";
	}

	private List<String> renderOrderings(List<WindowOrdering> orderings) {
		return orderings.stream().map(this::renderOrdering).toList();
	}

	private String renderOrdering(WindowOrdering ordering) {
		String expr = renderExpression(ordering.expression());
		return expr + ' ' + ordering.direction().name();
	}

	private void renderFrame(DefaultSqlBuilder builder, WindowFrame frame) {
		builder.append(frame.unit().name()).append(" BETWEEN ");
		renderBound(builder, frame.start());
		builder.append(" AND ");
		renderBound(builder, frame.end());
	}

	private void renderBound(DefaultSqlBuilder builder, WindowFrame.Bound bound) {
		if (bound == null) {
			return;
		}
		switch (bound.type()) {
		case UNBOUNDED_PRECEDING -> builder.append("UNBOUNDED PRECEDING");
		case UNBOUNDED_FOLLOWING -> builder.append("UNBOUNDED FOLLOWING");
		case CURRENT_ROW -> builder.append("CURRENT ROW");
		case PRECEDING -> builder.append(String.valueOf(bound.offset())).append(" PRECEDING");
		case FOLLOWING -> builder.append(String.valueOf(bound.offset())).append(" FOLLOWING");
		}
	}
}
