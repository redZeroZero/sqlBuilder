package org.in.media.res.sqlBuilder.implementation;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.ITranspilable;

public class Column implements IColumn, ITranspilable {

	private final String AS_ = " as ";

	private final String TABLE_SEP_ = ".";

	private String name;

	private String alias;

	private ITable table;

	private StringBuilder sb = new StringBuilder();

	public String transpile() {
		return this.transpile(true);
	}

	public String transpile(boolean useAlias) {
		reset();

		if (haveTableName(table))
			sb.append(tableName()).append(TABLE_SEP_).append(name);
		else
			sb.append(name);

		if (useAlias && haveColumnAlias(alias))
			sb.append(AS_).append(alias);

		return sb.toString();
	}

	private boolean haveTableName(ITable table) {
		String tableName = tableName();
		return tableName != null && !tableName.isBlank() && !tableName.isEmpty();
	}

	private boolean haveColumnAlias(String alias) {
		return alias != null && !alias.isEmpty() && !alias.isEmpty();
	}

	private String tableName() {
		return table.hasAlias() ? table.getAlias() : table.getName();
	}

	@Override
	public ITable table() {
		return this.table;
	}

	private void reset() {
		sb.setLength(0);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		Column c = null;

		public Builder() {
			c = new Column();
		}

		public Builder name(String name) {
			c.name = name;
			return this;
		}

		public Builder alias(String alias) {
			c.alias = alias;
			return this;
		}

		public Builder table(ITable table) {
			c.table = table;
			return this;
		}

		public Column build() {
			return c;
		}

	}

}
