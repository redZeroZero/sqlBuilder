package org.in.media.res.sqlBuilder.core.query;

import java.util.List;

import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.constants.JoinOperator;

/** Exposes raw FROM/JOIN fragments to transpilers without leaking implementations. */
public interface FromRawSupport {

	RawSqlFragment rawBaseFragment();

	List<RawJoinFragmentView> rawJoinFragments();

	interface RawJoinFragmentView {
		JoinOperator operator();

		RawSqlFragment fragment();
	}
}
