package org.in.media.res.sqlBuilder.core.query;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Condition;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.spi.From.JoinSpec;

public final class QueryValidator {

	private QueryValidator() {
	}

	public static ValidationReport validate(org.in.media.res.sqlBuilder.api.query.Query query, Dialect dialect) {
		ValidationReport report = ValidationReport.empty();
		if (query == null) {
			report.add(ValidationMessage.Severity.ERROR, "NULL_QUERY", "Query must not be null");
			return report;
		}
		if (!(query instanceof QueryImpl impl)) {
			return report;
		}
		checkProjectionAndGrouping(impl, report);
		checkDerivedTables(impl, report);
		checkOrderByGrouping(impl, report);
		checkParameters(impl, report);
		checkDialectRules(dialect, report);
		return report;
	}

	private static void checkProjectionAndGrouping(QueryImpl impl, ValidationReport report) {
		if (impl.aggColumns().isEmpty() || impl.columns().isEmpty()) {
			return;
		}
		Set<Column> grouped = new HashSet<>(impl.groupByColumnsView());
		for (Column col : impl.columns()) {
			if (!grouped.contains(col)) {
				report.add(ValidationMessage.Severity.WARNING, "GROUPING_MISMATCH",
						"Column " + col.getName() + " is selected but not grouped; some dialects require it in GROUP BY.");
			}
		}
	}

	private static void checkDerivedTables(QueryImpl impl, ValidationReport report) {
		for (Map.Entry<Table, JoinSpec> entry : impl.joins().entrySet()) {
			Table table = entry.getKey();
			if (table instanceof org.in.media.res.sqlBuilder.api.model.DerivedTable derived) {
				int projected = derived.subquery().aggColumns().size() + derived.subquery().columns().size();
				if (derived.subquery() instanceof QueryImpl qi) {
					projected += qi.projections().stream()
							.filter(p -> p.type() == SelectProjectionSupport.ProjectionType.RAW)
							.count();
				}
				if (table.getColumns().length != projected) {
					report.add(ValidationMessage.Severity.ERROR, "ALIAS_COUNT_MISMATCH",
							"Derived table " + table.getAlias() + " has " + table.getColumns().length
									+ " aliases but subquery projects " + projected);
				}
			}
		}
	}

	private static void checkOrderByGrouping(QueryImpl impl, ValidationReport report) {
		if (!impl.groupByColumnsView().isEmpty()) {
			for (var ordering : impl.orderingsView()) {
				Column col = ordering.column();
				if (col != null && !impl.groupByColumnsView().contains(col)
						&& !impl.aggColumns().containsKey(col)) {
					report.add(ValidationMessage.Severity.WARNING, "ORDER_BY_NOT_GROUPED",
							"ORDER BY " + col.getName() + " is not grouped/aggregated; prefer orderByAggregate/orderByAlias.");
				}
			}
		}
	}

	private static void checkParameters(QueryImpl impl, ValidationReport report) {
		var compiled = impl.compile();
		Set<SqlParameter<?>> seen = new HashSet<>();
		for (var ph : compiled.placeholders()) {
			SqlParameter<?> param = ph.parameter();
			if (param == null) {
				continue;
			}
			if (!seen.add(param)) {
				report.add(ValidationMessage.Severity.ERROR, "DUPLICATE_PARAM",
						"Parameter '" + param.name() + "' appears multiple times; use named binding via compile().bind(Map).");
			}
		}
	}

	private static void checkDialectRules(Dialect dialect, ValidationReport report) {
		// Placeholder for future dialect-specific rules.
		if (dialect == null) {
			return;
		}
	}
}
