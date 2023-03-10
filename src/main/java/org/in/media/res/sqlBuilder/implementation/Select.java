package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.implementation.factories.SelectTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.ISelect;
import org.in.media.res.sqlBuilder.interfaces.query.ISelectTranspiler;

public class Select implements ISelect {

	private List<IColumn> columns = new ArrayList<>();

	private Map<IColumn, AggregateOperator> aggColumns = new HashMap<>();

	ISelectTranspiler selectTranspiler = SelectTranspilerFactory.instanciateSelectTranspiler();

	public String transpile() {
		return selectTranspiler.transpile(this);
	}

	public void reset() {
		this.columns = null;
	}

	public ISelect select(IColumn column) {
		this.columns.add(column);
		return this;
	}

	public ISelect select(IColumn... columns) {
		for (IColumn c : columns)
			this.select(c);
		return this;
	}

	public ISelect select(ITable table) {
		this.select(table.getColumns());
		return this;
	}

	public ISelect select(AggregateOperator agg, IColumn column) {
		this.aggColumns().put(column, agg);
		return this;
	}

	public List<IColumn> columns() {
		return columns;
	}

	public Map<IColumn, AggregateOperator> aggColumns() {
		return aggColumns;
	}

}
