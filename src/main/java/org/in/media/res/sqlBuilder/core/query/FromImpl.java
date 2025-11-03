package org.in.media.res.sqlBuilder.core.query;

import java.util.LinkedHashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.JoinOperator;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.FromTranspiler;

public class FromImpl implements From {

	private Map<Table, Joiner> joins = new LinkedHashMap<>();

    private FromTranspiler fromTranspiler = TranspilerFactory.instanciateFromTranspiler();

    public Map<Table, Joiner> joins() {
        return joins;
    }

	public From from(Table table) {
		this.joins.put(table, null);
		return this;
	}

	public From from(Table... tables) {
		for (Table t : tables)
			this.from(t);
		return this;
	}

	public From join(Table t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.JOIN));
		return this;
	}

	public From innerJoin(Table t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.INNER_JOIN));
		return this;
	}

	public From leftJoin(Table t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.LEFT_JOIN));
		return this;
	}

	public From rightJoin(Table t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.RIGHT_JOIN));
		return this;
	}

	public From crossJoin(Table t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.CROSS_JOIN));
		return this;
	}

	public From fullOuterJoin(Table t) {
		this.from(t);
		joins.put(t, new Joiner(JoinOperator.FULL_OUTER_JOIN));
		return this;
	}

	public From on(Column c1, Column c2) {
		Joiner j = this.getFromKey(c1, c2);
		if (j != null) {
			j.setCol1(c1);
			j.setCol2(c2);
		}
		return this;
	}

	private Joiner getFromKey(Column c1, Column c2) {
		Joiner j = getConditionFromTableKey(c1.table());
		if (j != null)
			return j;
		j = getConditionFromTableKey(c2.table());
		if (j != null)
			return j;
		return null;
	}

	private Joiner getConditionFromTableKey(Table table) {
		return joins.get(table);
	}

	public String transpile() {
		return this.fromTranspiler.transpile(this);
	}

	public class Joiner {
		Column col1;
		Column col2;
		JoinOperator op;

		public JoinOperator getOp() {
			return op;
		}

		public void setOp(JoinOperator op) {
			this.op = op;
		}

		public Column getCol1() {
			return col1;
		}

		public Column getCol2() {
			return col2;
		}

		public Joiner(JoinOperator op) {
			this.op = op;
		}

		public void setCol1(Column col1) {
			this.col1 = col1;
		}

		public void setCol2(Column col2) {
			this.col2 = col2;
		}

	}

}
