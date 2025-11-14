package org.in.media.res.sqlBuilder.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.in.media.res.sqlBuilder.api.model.Column;
import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.Tables;
import org.in.media.res.sqlBuilder.api.query.Query;
import org.in.media.res.sqlBuilder.api.query.SqlQuery;
import org.junit.jupiter.api.Test;

class DerivedTableImplTest {

	private final Table employee = Tables.builder("Employee", "E")
			.column("ID")
			.column("SALARY")
			.build();

	private Query baseQuery() {
		return SqlQuery.newQuery().asQuery()
				.select(employee.get("ID"))
				.select(employee.get("SALARY"))
				.from(employee);
	}

	@Test
	void generatesAliasWhenMissingAndExposesColumns() {
		DerivedTableImpl derived = new DerivedTableImpl(baseQuery(), null, "EMP_ID", "EMP_SALARY");

		assertThat(derived.getAlias()).startsWith("SQ");
		assertThat(derived.getColumns()).hasSize(2);
		assertThat(derived.get("EMP_ID")).isNotNull();

		ColumnRef<Long> idRef = ColumnRef.of("EMP_ID", Long.class);
		Column resolved = derived.get(idRef);
		assertThat(resolved).isSameAs(derived.get("EMP_ID"));
		assertThat(derived.getName()).startsWith("(").contains("SELECT");
	}

	@Test
	void rejectsDuplicateColumnAliases() {
		assertThatThrownBy(() -> new DerivedTableImpl(baseQuery(), "alias", "COL", "COL"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Duplicate column alias");
	}

	@Test
	void usesSelectionAliasResolverWhenAliasesOmitted() {
		DerivedTableImpl derived = new DerivedTableImpl(baseQuery(), "avg_alias");

		assertThat(derived.getColumns()).hasSize(2);
		assertThat(derived.getColumns()[0].getName()).isNotBlank();
	}
}
