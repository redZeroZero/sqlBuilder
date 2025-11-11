package org.in.media.res.sqlBuilder.example;

import java.time.LocalDate;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.TableFacets;

/**
 * Companion implementation used by {@link TableFacets#columns(Class, Class)} to
 * provide typed Customer column handles.
 */
final class CustomerColumnsImpl implements CustomerColumns {

	private final TableFacets.Facet facet;

	private ColumnRef<Long> cached_ID;
	private ColumnRef<String> cached_FIRST_NAME;
	private ColumnRef<String> cached_LAST_NAME;
	private ColumnRef<String> cached_EMAIL;
	private ColumnRef<Boolean> cached_ACTIVE;
	private ColumnRef<LocalDate> cached_CREATED_AT;

	CustomerColumnsImpl(TableFacets.Facet facet) {
		this.facet = Objects.requireNonNull(facet, "facet");
	}

	@Override
	public ColumnRef<Long> ID() {
		if (cached_ID == null) {
			cached_ID = cast(facet.column("ID"));
		}
		return cached_ID;
	}

	@Override
	public ColumnRef<String> FIRST_NAME() {
		if (cached_FIRST_NAME == null) {
			cached_FIRST_NAME = cast(facet.column("FIRST_NAME"));
		}
		return cached_FIRST_NAME;
	}

	@Override
	public ColumnRef<String> LAST_NAME() {
		if (cached_LAST_NAME == null) {
			cached_LAST_NAME = cast(facet.column("LAST_NAME"));
		}
		return cached_LAST_NAME;
	}

	@Override
	public ColumnRef<String> EMAIL() {
		if (cached_EMAIL == null) {
			cached_EMAIL = cast(facet.column("EMAIL"));
		}
		return cached_EMAIL;
	}

	@Override
	public ColumnRef<Boolean> ACTIVE() {
		if (cached_ACTIVE == null) {
			cached_ACTIVE = cast(facet.column("ACTIVE"));
		}
		return cached_ACTIVE;
	}

	@Override
	public ColumnRef<LocalDate> CREATED_AT() {
		if (cached_CREATED_AT == null) {
			cached_CREATED_AT = cast(facet.column("CREATED_AT"));
		}
		return cached_CREATED_AT;
	}

	@SuppressWarnings("unchecked")
	private static <T> ColumnRef<T> cast(ColumnRef<?> ref) {
		return (ColumnRef<T>) ref;
	}
}
