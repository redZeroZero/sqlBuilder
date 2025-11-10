package org.in.media.res.sqlBuilder.core.query;

import java.util.List;

import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;

/** Internal contract exposing ORDER BY raw fragments to transpilers. */
public interface OrderByRawSupport {
	List<RawSqlFragment> orderByFragments();
}
