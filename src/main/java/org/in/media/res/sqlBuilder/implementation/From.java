package org.in.media.res.sqlBuilder.implementation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.JoinOperator;
import org.in.media.res.sqlBuilder.implementation.factories.FromTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.IFrom;
import org.in.media.res.sqlBuilder.interfaces.query.IFromTranspiler;

public class From implements IFrom {

	private Map<ITable, Joiner> joins = new LinkedHashMap<>();

	private IFromTranspiler fromTranspiler = FromTranspilerFactory.instanciateFromTranspiler();

	public Map<ITable, Joiner> joins() {
		return joins;
	}

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

	public String transpile() {
		return this.fromTranspiler.transpile(this);
	}

	public class Joiner {
		IColumn col1;
		IColumn col2;
		JoinOperator op;

		public JoinOperator getOp() {
			return op;
		}

		public void setOp(JoinOperator op) {
			this.op = op;
		}

		public IColumn getCol1() {
			return col1;
		}

		public IColumn getCol2() {
			return col2;
		}

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
