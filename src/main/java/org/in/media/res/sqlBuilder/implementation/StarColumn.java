package org.in.media.res.sqlBuilder.implementation;

import org.in.media.res.sqlBuilder.interfaces.model.IColumn;
import org.in.media.res.sqlBuilder.interfaces.model.ITable;

final class StarColumn implements IColumn {

	static final StarColumn INSTANCE = new StarColumn();

	private StarColumn() {
	}

	@Override
	public String transpile(boolean useAlias) {
		return "*";
	}

	@Override
	public String transpile() {
		return transpile(true);
	}

	@Override
	public ITable table() {
		return null;
	}
}
