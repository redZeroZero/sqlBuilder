package org.in.media.res.sqlBuilder.core.query;

import java.util.LinkedHashMap;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.JoinOperator;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.FromTranspiler;

final class FromImpl implements From {

	private final Map<Table, JoinSpec> joins = new LinkedHashMap<>();

    private FromTranspiler fromTranspiler = TranspilerFactory.instanciateFromTranspiler();

    public Map<Table, JoinSpec> joins() {
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
		joins.put(t, new JoinSpecImpl(JoinOperator.JOIN));
		return this;
	}

	public From innerJoin(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.INNER_JOIN));
		return this;
	}

	public From leftJoin(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.LEFT_JOIN));
		return this;
	}

	public From rightJoin(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.RIGHT_JOIN));
		return this;
	}

	public From crossJoin(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.CROSS_JOIN));
		return this;
	}

	public From fullOuterJoin(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.FULL_OUTER_JOIN));
		return this;
	}

	public From on(Column c1, Column c2) {
		JoinSpecImpl j = this.getFromKey(c1, c2);
		if (j != null) {
			j.setCol1(c1);
			j.setCol2(c2);
		}
		return this;
	}

	private JoinSpecImpl getFromKey(Column c1, Column c2) {
		JoinSpecImpl j = getConditionFromTableKey(c1.table());
		if (j != null)
			return j;
		j = getConditionFromTableKey(c2.table());
		if (j != null)
			return j;
		return null;
	}

	private JoinSpecImpl getConditionFromTableKey(Table table) {
		return (JoinSpecImpl) joins.get(table);
	}

	public String transpile() {
		return this.fromTranspiler.transpile(this);
	}

	private static final class JoinSpecImpl implements JoinSpec {
		private Column col1;
		private Column col2;
		private final JoinOperator op;

		@Override
		public JoinOperator getOp() {
			return op;
		}

		@Override
		public Column getCol1() {
			return col1;
		}

		@Override
		public Column getCol2() {
			return col2;
		}

		JoinSpecImpl(JoinOperator op) {
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
