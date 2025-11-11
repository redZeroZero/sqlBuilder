package org.in.media.res.sqlBuilder.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.model.TableFacets;
import org.in.media.res.sqlBuilder.core.model.samples.AnnotatedCustomer;
import org.in.media.res.sqlBuilder.core.model.samples.EnumEmployee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SchemaScannerTest {

	private static final String BASE_PACKAGE = "org.in.media.res.sqlBuilder.core.model.samples";

	private ClassLoader classLoader;

	@BeforeEach
	void resetCache() {
		classLoader = Thread.currentThread().getContextClassLoader();
		SchemaScanner.invalidate(BASE_PACKAGE, classLoader);
	}

	@Test
	void scanFindsAnnotatedTablesAndBindsColumnRefs() {
		List<Table> tables = SchemaScanner.scan(BASE_PACKAGE, classLoader);
		assertThat(tables).extracting(Table::getName).contains("Customer", "Orders", "EnumEmployee");

		Table customer = tables.stream().filter(t -> t.getName().equals("Customer")).findFirst().orElseThrow();
		assertThat(customer.getAlias()).isEqualTo("C");
		assertThat(customer.getColumns()).hasSize(3);
		assertThat(customer.get("FIRST_NAME")).isNotNull();

		// ColumnRef fields are bound during scanning
		assertThat(AnnotatedCustomer.ID.column()).isNotNull();
		assertThat(AnnotatedCustomer.FIRST_NAME.column().getAlias()).isEqualTo("firstName");
	}

	@Test
	void facetsExposeTypedColumnsAndRowBuilders() {
		TableFacets facets = SchemaScanner.facets(BASE_PACKAGE, classLoader);
		TableFacets.Facet facet = facets.facetFor(AnnotatedCustomer.class);

		assertThat(facet.column("first_name")).isSameAs(AnnotatedCustomer.FIRST_NAME);
		var row = facet.rowBuilder()
				.set(AnnotatedCustomer.ID, 900L)
				.set(AnnotatedCustomer.FIRST_NAME, "Grace")
				.build();
		assertThat(row.get(AnnotatedCustomer.FIRST_NAME)).isEqualTo("Grace");

		assertThrows(NullPointerException.class, () -> facets.facetFor(String.class));
	}

	@Test
	void enumDescriptorsAreConvertedToTables() {
		List<Table> tables = SchemaScanner.scan(BASE_PACKAGE, classLoader);
		Table descriptorTable = tables.stream().filter(t -> t.getName().equals("EnumEmployee")).findFirst()
				.orElseThrow();

		assertThat(descriptorTable.hasAlias()).isTrue();
		assertThat(descriptorTable.getAlias()).isEqualTo("EE");
		assertThat(EnumEmployee.ID.column()).isNotNull();
	}
}
