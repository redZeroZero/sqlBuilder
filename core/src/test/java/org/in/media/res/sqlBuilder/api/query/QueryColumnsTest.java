package org.in.media.res.sqlBuilder.api.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.in.media.res.sqlBuilder.api.model.ColumnRef;
import org.in.media.res.sqlBuilder.api.model.ScannedSchema;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.core.model.samples.AnnotatedCustomer;
import org.in.media.res.sqlBuilder.core.model.samples.AnnotatedCustomerColumns;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryColumnsTest {

	private static final String BASE_PACKAGE = "org.in.media.res.sqlBuilder.core.model.samples";

	@BeforeEach
	void resetSchemaCache() {
		ScannedSchema.clearCache();
	}

	@Test
	void bundlesTableAndColumnsForDescriptor() {
		ScannedSchema schema = new ScannedSchema(BASE_PACKAGE);
		QueryColumns<CustomColumns> bundle = QueryColumns.of(schema, AnnotatedCustomer.class, CustomColumns.class);

		Table table = bundle.table();
		assertThat(table.getName()).isEqualTo("Customer");
		assertThat(bundle.columns().ID()).isSameAs(AnnotatedCustomer.ID);
	}

	@Test
	void infersDescriptorFromColumnsSuffix() {
		ScannedSchema schema = new ScannedSchema(BASE_PACKAGE);
		QueryColumns<AnnotatedCustomerColumns> bundle = QueryColumns.of(schema, AnnotatedCustomerColumns.class);

		assertThat(bundle.table().getAlias()).isEqualTo("C");
		assertThat(bundle.columns().FIRST_NAME()).isSameAs(AnnotatedCustomer.FIRST_NAME);
	}

	@Test
	void failsWhenDescriptorCannotBeResolved() {
		ScannedSchema schema = new ScannedSchema(BASE_PACKAGE);

		assertThatThrownBy(() -> QueryColumns.of(schema, OrphanColumns.class))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Unable to load descriptor class");
	}

	interface CustomColumns {
		ColumnRef<Long> ID();
	}

	interface OrphanColumns {
		ColumnRef<Long> ID();
	}
}
