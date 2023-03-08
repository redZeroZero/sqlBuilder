package org.in.media.res.sqlBuilder.implementation;

import java.util.HashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.JoinOperator;
import org.in.media.res.sqlBuilder.constants.Operator;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.IFrom;

public class From implements IFrom {

	private StringBuilder sb = new StringBuilder();

	private String SEP_ = ", ";

	private String FROM_ = " FROM ";

	private final String ALIAS_SEP_ = " ";

	private Map<ITable, Joiner> joins = new HashMap<>();
	
	public IFrom from(ITable table) {
		this.joins.put(table, null);
		return this;
	}

	public IFrom from(ITable... tables) {
		for (ITable t : tables)
			this.from(t);
		return this;
	}

	public IFrom join(ITable t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.JOIN));
		return this;
	}

	public IFrom innerJoin(ITable t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.INNER_JOIN));
		return this;
	}

	public IFrom leftJoin(ITable t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.LEFT_JOIN));
		return this;
	}

	public IFrom rightJoin(ITable t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.RIGHT_JOIN));
		return this;
	}

	public IFrom on(IColumn c1, IColumn c2) {
		Joiner j = this.getFromKey(c1, c2);
		if (j != null) {
			j.setCol1(c1);
			j.setCol2(c2);
		}
		return this;
	}
	public String transpile() {
		resetBuilder();
		sb.append(FROM_);
		if (haveData()) {
			Joiner[] values = joins.values().stream().filter(v -> v != null).toArray(Joiner[]::new);
			for (int i = 0; i < values.length; i++) {
				this.buildItem(values, i);
			}
		}
		return sb.toString();
	}

	private boolean haveData() {
		return joins.size() > 0;
	}

	private boolean isLastItemOf(Object[] values, int i) {
		return values.length - 1 == i;
	}

	private void buildItem(Joiner[] values, int i) {
		IColumn col1 = values[i].col1;
		IColumn col2 = values[i].col2;
		buildTableDetail(col1.table());
		sb.append(values[i].op.value());
		buildTableDetail(col2.table());
		sb.append(JoinOperator.ON.value()).append(col1.transpile(false)).append(Operator.EQ.value())
				.append(col2.transpile(false));
		if (!isLastItemOf(values, i))
			sb.append(SEP_);
	}

	private void buildTableDetail(ITable t) {
		sb.append(t.getName());
		if (t.hasAlias())
			sb.append(ALIAS_SEP_).append(t.getAlias());
	}

	private Joiner getFromKey(IColumn c1, IColumn c2) {
		Joiner j = getConditionFromTableKey(c1.table());
		if (j != null)
			return j;
		j = getConditionFromTableKey(c2.table());
		if (j != null)
			return j;
		return null;
	}

	private Joiner getConditionFromTableKey(ITable table) {
		return joins.get(table);
	}

	public void reset() {
		resetBuilder();
	}

	private void resetBuilder() {
		sb.setLength(0);
	}

	private class Joiner {
		IColumn col1;
		IColumn col2;
		JoinOperator op;

		public Joiner(JoinOperator op) {
			this.op = op;
		}

		public void setCol1(IColumn col1) {
			this.col1 = col1;
		}

		public void setCol2(IColumn col2) {
			this.col2 = col2;
		}

	}

}
