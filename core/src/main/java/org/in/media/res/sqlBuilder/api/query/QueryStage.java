package org.in.media.res.sqlBuilder.api.query;

/**
 * Common contract for staged query views. Implementations are expected to also
 * implement {@link Query}, allowing callers to widen to the full API via
 * {@link #asQuery()} when needed.
 */
public interface QueryStage {

	default Query asQuery() {
		return (Query) this;
	}

}
