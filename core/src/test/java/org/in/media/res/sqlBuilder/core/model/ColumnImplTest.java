package org.in.media.res.sqlBuilder.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.core.query.dialect.DialectContext;
import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;
import org.junit.jupiter.api.Test;

class ColumnImplTest {

	@Test
	void transpileIncludesAliasWhenRequested() {
		Table table = Tables.builder("Employee", "E")
				.column("FIRST_NAME", "firstName")
				.build();

		Column column = table.get("FIRST_NAME");
		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.postgres())) {
			assertThat(column.transpile()).isEqualTo("\"E\".\"FIRST_NAME\" as \"firstName\"");
			assertThat(column.transpile(false)).isEqualTo("\"E\".\"FIRST_NAME\"");
		}
	}

	@Test
	void columnDescriptorBindingUsesColumnAlias() {
		ColumnRef<Long> id = ColumnRef.of("ID", "id", Long.class);
		Table table = Tables.builder("Employee", null)
				.column(id)
				.build();

		try (DialectContext.Scope ignored = DialectContext.scope(Dialects.oracle())) {
			assertThat(id.column().transpile(false)).isEqualTo("\"Employee\".\"ID\"");
		}
	}
}
