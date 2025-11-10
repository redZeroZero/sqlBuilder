package org.in.media.res.sqlBuilder.core.query;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;

/**
 * Internal projection snapshot exposed to transpilers without leaking concrete implementations.
 */
public interface SelectProjectionSupport {

	List<SelectProjection> projections();

	interface SelectProjection {
		ProjectionType type();

		Column column();

		AggregateOperator aggregate();

		RawSqlFragment fragment();
	}

	enum ProjectionType {
		COLUMN,
		AGGREGATE,
		RAW
	}
}
