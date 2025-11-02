package org.in.media.res.sqlBuilder.api.query;

import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.constants.AggregateOperator;
import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableDescriptor;

public interface Select extends Clause, Resetable, Transpilable {

	Select select(Column column);

	Select select(TableDescriptor<?> descriptor);

	Select select(Column... columns);

	Select select(TableDescriptor<?>... descriptors);

	Select select(Table table);

	Select select(AggregateOperator agg, Column column);

	Select select(AggregateOperator agg, TableDescriptor<?> descriptor);

	List<Column> columns();

	Map<Column, AggregateOperator> aggColumns();
	
}
