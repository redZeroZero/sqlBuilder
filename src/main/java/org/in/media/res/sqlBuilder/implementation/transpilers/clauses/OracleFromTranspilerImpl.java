package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import static org.in.media.res.sqlBuilder.constants.JoinOperator.ON;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;

import org.in.media.res.sqlBuilder.implementation.From.Joiner;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.IFrom;
import org.in.media.res.sqlBuilder.interfaces.query.IFromTranspiler;

public class OracleFromTranspilerImpl implements IFromTranspiler {

	private String SEP_ = ", ";

	private String FROM_ = " FROM ";

	private final String ALIAS_SEP_ = " ";

	@Override
	public String transpile(IFrom f) {
		StringBuilder sb = new StringBuilder();
		sb.append(FROM_);
		if (haveData(f)) {
			Joiner[] values = f.joins().values().stream().filter(v -> v != null).toArray(Joiner[]::new);
			for (int i = 0; i < values.length; i++) {
				this.buildItem(f, sb, values, i);
			}
		}
		return sb.toString();
	}

	private boolean haveData(IFrom f) {
		return f.joins().size() > 0;
	}

	private boolean isLastItemOf(Object[] values, int i) {
		return values.length - 1 == i;
	}

	private void buildItem(IFrom f, StringBuilder sb, Joiner[] values, int i) {
		IColumn col1 = values[i].getCol1();
		IColumn col2 = values[i].getCol2();
		buildTableDetail(sb, col1.table());
		sb.append(values[i].getOp().value());
		buildTableDetail(sb, col2.table());
		sb.append(ON.value()).append(col1.transpile(false)).append(EQ.value()).append(col2.transpile(false));
		if (!isLastItemOf(values, i))
			sb.append(SEP_);
	}

	private void buildTableDetail(StringBuilder sb, ITable t) {
		sb.append(t.getName());
		if (t.hasAlias())
			sb.append(ALIAS_SEP_).append(t.getAlias());
	}

}
