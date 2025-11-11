package org.in.media.res.sqlBuilder.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.junit.jupiter.api.Test;

class TableImplTest {

	@Test
	void builderRegistersColumnsAndSchema() {
		TableImpl<?> table = new TableImpl<>("Employee", "E", builder -> {
			builder.column("ID");
			builder.column("FIRST_NAME", "firstName");
		});
		table.includeSchema("HR");

		assertThat(table.getName()).isEqualTo("Employee");
		assertThat(table.getAlias()).isEqualTo("E");
		assertThat(table.getSchema()).isEqualTo("HR");
		assertThat(table.hasAlias()).isTrue();
		assertThat(table.hasTableName()).isTrue();
		assertThat(table.getColumns()).hasSize(2);
		assertThat(table.get("FIRST_NAME").hasColumnAlias()).isTrue();
	}

	@Test
	void columnRefDescriptorsAreBound() {
		ColumnRef<Long> id = ColumnRef.of("ID", Long.class);
		TableImpl<?> table = new TableImpl<>("Employee", null, builder -> builder.column(id));

		Column column = table.get("ID");
		assertThat(id.column()).isSameAs(column);
		assertThat(column.table()).isEqualTo(table);
	}
}
