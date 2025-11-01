package org.in.media.res.sqlBuilder.implementation;

import org.in.media.res.sqlBuilder.implementation.factories.LimitTranspilerFactory;
import org.in.media.res.sqlBuilder.interfaces.query.ILimit;
import org.in.media.res.sqlBuilder.interfaces.query.ILimitTranspiler;

public class Limit implements ILimit {

	private Integer limit;
	private Integer offset;

	private final ILimitTranspiler limitTranspiler = LimitTranspilerFactory.instanciateLimitTranspiler();

	@Override
	public String transpile() {
		if (limit == null && offset == null) {
			return "";
		}
		return limitTranspiler.transpile(this);
	}

	@Override
	public void reset() {
		limit = null;
		offset = null;
	}

	@Override
	public ILimit limit(int limit) {
		this.limit = limit;
		return this;
	}

	@Override
	public ILimit offset(int offset) {
		this.offset = offset;
		return this;
	}

	@Override
	public ILimit limitAndOffset(int limit, int offset) {
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
