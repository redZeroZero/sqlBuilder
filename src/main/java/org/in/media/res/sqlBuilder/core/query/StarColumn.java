package org.in.media.res.sqlBuilder.core.query;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;

final class StarColumn implements Column {

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
	public Table table() {
		return null;
	}

	@Override
	public String getName() {
		return "*";
	}

	@Override
	public String getAlias() {
		return null;
	}

	@Override
	public boolean hasColumnAlias() {
		return false;
	}
}
