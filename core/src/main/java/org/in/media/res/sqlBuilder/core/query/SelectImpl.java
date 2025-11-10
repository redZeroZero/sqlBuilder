package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.SelectTranspiler;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;

final class SelectImpl implements Select, SelectProjectionSupport {

	private static final class Entry implements SelectProjection {
		private final ProjectionType type;
		private final Column column;
		private final AggregateOperator aggregate;
		private final RawSqlFragment fragment;

		private Entry(ProjectionType type, Column column, AggregateOperator aggregate, RawSqlFragment fragment) {
			this.type = type;
			this.column = column;
			this.aggregate = aggregate;
			this.fragment = fragment;
		}

		static Entry column(Column column) {
			return new Entry(ProjectionType.COLUMN, column, null, null);
		}

		static Entry aggregate(AggregateOperator aggregate, Column column) {
			return new Entry(ProjectionType.AGGREGATE, column, aggregate, null);
		}

		static Entry raw(RawSqlFragment fragment) {
			return new Entry(ProjectionType.RAW, null, null, fragment);
		}

		@Override
		public ProjectionType type() {
			return type;
		}

		@Override
		public Column column() {
			return column;
		}

		@Override
		public AggregateOperator aggregate() {
			return aggregate;
		}

		@Override
		public RawSqlFragment fragment() {
			return fragment;
		}
	}

	private final List<Entry> entries = new ArrayList<>();
	private final List<String> hints = new ArrayList<>();
	private boolean distinct;

	private final SelectTranspiler selectTranspiler = TranspilerFactory.instanciateSelectTranspiler();

	@Override
	public String transpile() {
		return selectTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		entries.clear();
		hints.clear();
		distinct = false;
	}

	@Override
	public Select select(Column column) {
		entries.add(Entry.column(column));
		return this;
	}

	@Override
	public Select select(TableDescriptor<?> descriptor) {
		return select(descriptor.column());
	}

	@Override
	public Select select(Column... columns) {
		for (Column column : columns) {
			select(column);
		}
		return this;
	}

	@Override
	public Select select(TableDescriptor<?>... descriptors) {
		for (TableDescriptor<?> descriptor : descriptors) {
			select(descriptor);
		}
		return this;
	}

	@Override
	public Select select(Table table) {
		return select(table.getColumns());
	}

	@Override
	public Select select(AggregateOperator agg, Column column) {
		entries.add(Entry.aggregate(agg, column));
		return this;
	}

	@Override
	public Select select(AggregateOperator agg, TableDescriptor<?> descriptor) {
		return select(agg, descriptor.column());
	}

	@Override
	public Select selectRaw(String sql) {
		return selectRaw(RawSql.of(sql));
	}

	@Override
	public Select selectRaw(String sql, SqlParameter<?>... params) {
		return selectRaw(RawSql.of(sql, params));
	}

	@Override
	public Select selectRaw(RawSqlFragment fragment) {
		entries.add(Entry.raw(fragment));
		return this;
	}

	@Override
	public List<Column> columns() {
		List<Column> cols = new ArrayList<>();
		for (Entry entry : entries) {
			if (entry.type == ProjectionType.COLUMN) {
				cols.add(entry.column);
			}
		}
		return cols;
	}

	@Override
	public Map<Column, AggregateOperator> aggColumns() {
		Map<Column, AggregateOperator> aggregates = new LinkedHashMap<>();
		for (Entry entry : entries) {
			if (entry.type == ProjectionType.AGGREGATE) {
				aggregates.put(entry.column, entry.aggregate);
			}
		}
		return aggregates;
	}

	@Override
	public Select distinct() {
		this.distinct = true;
		return this;
	}

	@Override
	public boolean isDistinct() {
		return distinct;
	}

	@Override
	public Select hint(String hintSql) {
		if (hintSql != null && !hintSql.isBlank()) {
			hints.add(hintSql);
		}
		return this;
	}

	@Override
	public List<String> hints() {
		return List.copyOf(hints);
	}

	@Override
	public List<SelectProjection> projections() {
		return List.copyOf(entries);
	}
}
