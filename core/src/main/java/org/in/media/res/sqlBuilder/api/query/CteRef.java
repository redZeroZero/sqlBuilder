package org.in.media.res.sqlBuilder.api.query;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.core.model.ColumnRef;

/**
 * Table-like reference returned when registering a Common Table Expression (CTE).
 */
public interface CteRef extends Table {

	/**
	 * Resolve a column inside the CTE by its alias/name.
	 *
	 * @param aliasOrName the column alias declared for the CTE
	 * @return the {@link Column} bound to the alias
	 */
	default Column column(String aliasOrName) {
		Column column = get(aliasOrName);
		if (column == null) {
			throw new IllegalArgumentException("Unknown column '" + aliasOrName + "' in CTE '" + getName() + "'");
		}
		return column;
	}

	/**
	 * Resolve a {@link ColumnRef} descriptor for the given alias.
	 *
	 * @param aliasOrName the alias declared for the CTE column
	 * @return a descriptor bound to the matching column
	 */
	default ColumnRef<?> col(String aliasOrName) {
		Column column = column(aliasOrName);
		ColumnRef<?> ref = ColumnRef.of(aliasOrName);
		ref.bindColumn(column);
		return ref;
	}

	@Override
	Column get(String columnName);

	@Override
	Column get(TableDescriptor<?> descriptor);
}
