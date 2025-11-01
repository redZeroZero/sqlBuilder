package org.in.media.res.sqlBuilder.implementation.transpilers.clauses;

import static org.in.media.res.sqlBuilder.constants.JoinOperator.ON;
import static org.in.media.res.sqlBuilder.constants.Operator.EQ;

import java.util.Map;

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
		if (f.joins().isEmpty()) {
			return "";
		}
		if (f.joins().values().stream().noneMatch(joiner -> joiner == null)) {
			throw new IllegalStateException("FROM clause requires at least one base table before joins");
		}
		StringBuilder sb = new StringBuilder();
		sb.append(FROM_);
		boolean firstBaseTableAppended = false;
		for (Map.Entry<ITable, Joiner> entry : f.joins().entrySet()) {
			ITable table = entry.getKey();
			Joiner joiner = entry.getValue();
			if (joiner == null) {
				if (firstBaseTableAppended) {
					sb.append(SEP_);
				}
				buildTableDetail(sb, table);
				firstBaseTableAppended = true;
			} else {
				sb.append(joiner.getOp().value());
				buildTableDetail(sb, table);
				IColumn col1 = joiner.getCol1();
				IColumn col2 = joiner.getCol2();
				if (col1 == null || col2 == null) {
					throw new IllegalStateException("JOIN on table " + table.getName()
							+ " must define both columns via on(column1, column2)");
				}
				sb.append(ON.value()).append(col1.transpile(false)).append(EQ.value()).append(col2.transpile(false));
			}
		}
		return sb.toString();
	}

	private void buildTableDetail(StringBuilder sb, ITable t) {
		sb.append(t.getName());
		if (t.hasAlias())
			sb.append(ALIAS_SEP_).append(t.getAlias());
	}

}
