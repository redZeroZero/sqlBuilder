package org.in.media.res.sqlBuilder.core.query;

import org.in.media.res.sqlBuilder.api.query.spi.Limit;
final class LimitImpl implements Limit {

	private Integer limit;
	private Integer offset;

	@Override
	public String transpile() {
		return "";
	}

	@Override
	public void reset() {
		limit = null;
		offset = null;
	}

	@Override
	public Limit limit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public Limit offset(int offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public Limit limitAndOffset(int limit, int offset) {
		this.limit = limit;
		this.offset = offset;
		return this;
	}

	@Override
	public Integer limitValue() {
		return limit;
	}

	@Override
	public Integer offsetValue() {
		return offset;
	}
}
