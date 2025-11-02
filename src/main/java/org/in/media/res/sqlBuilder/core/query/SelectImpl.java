package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.core.query.factory.SelectTranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.Select;
import org.in.media.res.sqlBuilder.api.query.SelectTranspiler;

public class SelectImpl implements Select {

	private List<Column> columns = new ArrayList<>();

	private Map<Column, AggregateOperator> aggColumns = new LinkedHashMap<>();

	SelectTranspiler selectTranspiler = SelectTranspilerFactory.instanciateSelectTranspiler();

	public String transpile() {
		return selectTranspiler.transpile(this);
	}

	public void reset() {
		this.columns.clear();
		this.aggColumns.clear();
	}

	public Select select(Column column) {
		this.columns.addLast(column);
		return this;
	}

	@Override
	public Select select(TableDescriptor<?> descriptor) {
		return this.select(descriptor.column());
	}

	public Select select(Column... columns) {
		for (Column c : columns)
			this.select(c);
		return this;
	}

	@Override
	public Select select(TableDescriptor<?>... descriptors) {
		for (TableDescriptor<?> descriptor : descriptors) {
			this.select(descriptor);
		}
		return this;
	}

	public Select select(Table table) {
		this.select(table.getColumns());
		return this;
	}

	public Select select(AggregateOperator agg, Column column) {
		this.aggColumns().put(column, agg);
		return this;
	}

	@Override
	public Select select(AggregateOperator agg, TableDescriptor<?> descriptor) {
		return this.select(agg, descriptor.column());
	}

	public List<Column> columns() {
		return columns;
	}

	public Map<Column, AggregateOperator> aggColumns() {
		return aggColumns;
	}

}
