package org.in.media.res.sqlBuilder.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.ISelect;

public class Select implements ISelect {

	private String SEP_ = ", ";
	
	private String EMPTY_STR = "";

	private String SELECT_ = "SELECT ";

	private String OP_PARENTHESIS = "(";

	private String CL_PARENTHESIS = ")";

	private List<IColumn> columns = new ArrayList<>();

	private Map<IColumn, AggregateOperator> aggColumns = new HashMap<>();

	private StringBuilder sb = new StringBuilder();

	public String transpile() {
		resetBuilder();
		sb.append(SELECT_);

		if (!aggColumns.isEmpty())
			transpileAggregates();
		transpileColumns();

		return sb.toString();
	}

	private void transpileColumns() {
		for (int i = 0; i < columns.size(); i++) {
			if ((columns.size() - 1 == i))
				sb.append(columns.get(i).transpile());
			else
				sb.append(columns.get(i).transpile()).append(SEP_);
		}
	}

	private void transpileAggregates() {
		IColumn[] arr = aggColumns.keySet().toArray(new IColumn[0]);
		for (IColumn col : arr) {
			sb.append(aggColumns.get(col).value()).append(OP_PARENTHESIS).append(col.transpile(false))
					.append(CL_PARENTHESIS);
			if (col.equals(arr[arr.length - 1]) && columns.isEmpty())
				sb.append(EMPTY_STR);
			else
				sb.append(SEP_);
		}
	}

	public void reset() {
		columns = null;
		resetBuilder();
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
		aggColumns.put(column, agg);
		return this;
	}

	private void resetBuilder() {
		sb.setLength(0);
	}

}
