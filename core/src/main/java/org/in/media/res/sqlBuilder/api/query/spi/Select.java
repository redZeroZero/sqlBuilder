package org.in.media.res.sqlBuilder.api.query.spi;

import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;
import org.in.media.res.sqlBuilder.api.query.RawSqlFragment;
import org.in.media.res.sqlBuilder.api.query.SqlParameter;
import org.in.media.res.sqlBuilder.constants.AggregateOperator;

public interface Select extends Clause, Resetable, Transpilable {

	Select select(Column column);

	Select distinct();

	Select select(TableDescriptor<?> descriptor);

	Select select(Column... columns);

	Select select(TableDescriptor<?>... descriptors);

	Select select(Table table);

	Select select(AggregateOperator agg, Column column);

	Select select(AggregateOperator agg, TableDescriptor<?> descriptor);

	Select selectRaw(String sql);

	Select selectRaw(String sql, SqlParameter<?>... params);

	Select selectRaw(RawSqlFragment fragment);

	List<Column> columns();

	Map<Column, AggregateOperator> aggColumns();

	boolean isDistinct();

	Select hint(String hintSql);

	List<String> hints();
	
}
