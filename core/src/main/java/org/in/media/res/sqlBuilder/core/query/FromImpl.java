package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.From;
import org.in.media.res.sqlBuilder.api.query.FromTranspiler;
import org.in.media.res.sqlBuilder.api.query.RawSql;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.constants.JoinOperator;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;

final class FromImpl implements From, FromRawSupport {

	private final Map<Table, JoinSpec> joins = new LinkedHashMap<>();
	private RawSqlFragment rawBase;
	private final List<RawJoinFragment> rawJoins = new ArrayList<>();

    private FromTranspiler fromTranspiler = TranspilerFactory.instanciateFromTranspiler();

    public Map<Table, JoinSpec> joins() {
        return joins;
    }

	public From from(Table table) {
		if (rawBase != null) {
			throw new IllegalStateException("fromRaw() already configured; cannot mix typed tables");
		}
		this.joins.put(table, null);
		return this;
	}

	public From from(Table... tables) {
		for (Table t : tables)
			this.from(t);
		return this;
	}

	@Override
	public From fromRaw(String sql) {
		return fromRaw(RawSql.of(sql));
	}

	@Override
	public From fromRaw(String sql, SqlParameter<?>... params) {
		return fromRaw(RawSql.of(sql, params));
	}

	@Override
	public From fromRaw(RawSqlFragment fragment) {
		if (!joins.isEmpty() || rawBase != null) {
			throw new IllegalStateException("fromRaw() cannot be combined with typed FROM entries");
		}
		this.rawBase = fragment;
		return this;
	}

	public From join(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.JOIN));
		return this;
	}

	@Override
	public From joinRaw(String sql) {
		return joinRaw(RawSql.of(sql));
	}

	@Override
	public From joinRaw(String sql, SqlParameter<?>... params) {
		return joinRaw(RawSql.of(sql, params));
	}

	@Override
	public From joinRaw(RawSqlFragment fragment) {
		rawJoins.add(new RawJoinFragment(JoinOperator.JOIN, fragment));
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

	@Override
	public From leftJoinRaw(String sql) {
		return leftJoinRaw(RawSql.of(sql));
	}

	@Override
	public From leftJoinRaw(String sql, SqlParameter<?>... params) {
		return leftJoinRaw(RawSql.of(sql, params));
	}

	@Override
	public From leftJoinRaw(RawSqlFragment fragment) {
		rawJoins.add(new RawJoinFragment(JoinOperator.LEFT_JOIN, fragment));
		return this;
	}

	public From rightJoin(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.RIGHT_JOIN));
		return this;
	}

	@Override
	public From rightJoinRaw(String sql) {
		return rightJoinRaw(RawSql.of(sql));
	}

	@Override
	public From rightJoinRaw(String sql, SqlParameter<?>... params) {
		return rightJoinRaw(RawSql.of(sql, params));
	}

	@Override
	public From rightJoinRaw(RawSqlFragment fragment) {
		rawJoins.add(new RawJoinFragment(JoinOperator.RIGHT_JOIN, fragment));
		return this;
	}

	public From crossJoin(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.CROSS_JOIN));
		return this;
	}

	@Override
	public From crossJoinRaw(String sql) {
		return crossJoinRaw(RawSql.of(sql));
	}

	@Override
	public From crossJoinRaw(String sql, SqlParameter<?>... params) {
		return crossJoinRaw(RawSql.of(sql, params));
	}

	@Override
	public From crossJoinRaw(RawSqlFragment fragment) {
		rawJoins.add(new RawJoinFragment(JoinOperator.CROSS_JOIN, fragment));
		return this;
	}

	public From fullOuterJoin(Table t) {
		this.from(t);
		joins.put(t, new JoinSpecImpl(JoinOperator.FULL_OUTER_JOIN));
		return this;
	}

	@Override
	public From fullOuterJoinRaw(String sql) {
		return fullOuterJoinRaw(RawSql.of(sql));
	}

	@Override
	public From fullOuterJoinRaw(String sql, SqlParameter<?>... params) {
		return fullOuterJoinRaw(RawSql.of(sql, params));
	}

	@Override
	public From fullOuterJoinRaw(RawSqlFragment fragment) {
		rawJoins.add(new RawJoinFragment(JoinOperator.FULL_OUTER_JOIN, fragment));
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

	@Override
	public RawSqlFragment rawBaseFragment() {
		return rawBase;
	}

	@Override
	public List<RawJoinFragmentView> rawJoinFragments() {
		return List.copyOf(rawJoins);
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

	static final class RawJoinFragment implements RawJoinFragmentView {
		private final JoinOperator operator;
		private final RawSqlFragment fragment;

		RawJoinFragment(JoinOperator operator, RawSqlFragment fragment) {
			this.operator = operator;
			this.fragment = fragment;
		}

		@Override
		public JoinOperator operator() {
			return operator;
		}

		@Override
		public RawSqlFragment fragment() {
			return fragment;
		}
	}
}
