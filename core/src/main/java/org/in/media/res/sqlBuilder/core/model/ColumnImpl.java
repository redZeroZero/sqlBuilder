package org.in.media.res.sqlBuilder.core.model;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.spi.ColumnTranspiler;
import org.in.media.res.sqlBuilder.core.query.factory.TranspilerFactory;

public class ColumnImpl implements Column {

	private String name;

	private String alias;

	private Table table;

	private ColumnTranspiler transpiler = TranspilerFactory.instantiateColumnTranspiler();

	public String transpile() {
		return this.transpile(true);
	}

	public String transpile(boolean useAlias) {
		return transpiler.transpile(useAlias, this);
	}

	@Override
	public Table table() {
		return this.table;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public boolean hasColumnAlias() {
		return this.alias != null && !this.alias.isBlank();
	}

    public static class Builder {

        ColumnImpl c = null;

        public Builder() {
            c = new ColumnImpl();
        }

		public Builder name(String name) {
			c.setName(name);
			return this;
		}

		public Builder alias(String alias) {
			c.setAlias(alias);
			return this;
		}

		public Builder table(Table table) {
			c.table = table;
			return this;
		}

		public ColumnImpl build() {
			return c;
		}

	}

}
