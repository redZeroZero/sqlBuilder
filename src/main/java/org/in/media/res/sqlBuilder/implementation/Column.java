package org.in.media.res.sqlBuilder.implementation;

import org.in.media.res.sqlBuilder.implementation.factories.TranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;
import org.in.media.res.sqlBuilder.interfaces.query.IColumnTranspiler;
import org.in.media.res.sqlBuilder.interfaces.query.ITranspilable;

public class Column implements IColumn, ITranspilable {

	private String name;

	private String alias;

	private ITable table;

	private IColumnTranspiler transpiler = TranspilerFactory.instanciateColumnTranspiler();

	public String transpile() {
		return this.transpile(true);
	}

	public String transpile(boolean useAlias) {
		return transpiler.transpile(useAlias, this);
	}

	@Override
	public ITable table() {
		return this.table;
	}

	public static Builder builder() {
		return new Builder();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public boolean hasColumnAlias() {
		return this.alias != null && !this.alias.isBlank();
	}

	public static class Builder {

		Column c = null;

		public Builder() {
			c = new Column();
		}

		public Builder name(String name) {
			c.setName(name);
			return this;
		}

		public Builder alias(String alias) {
			c.setAlias(alias);
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
