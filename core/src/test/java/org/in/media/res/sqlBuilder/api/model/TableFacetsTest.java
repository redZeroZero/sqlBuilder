package org.in.media.res.sqlBuilder.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.model.TableFacets.Facet;
import org.in.media.res.sqlBuilder.api.model.annotation.SqlColumn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Covers the runtime typed-column facade (TableFacets + TableRow).
 */
public class TableFacetsTest {

	private static TableFacets facets;
	private static Facet employeeFacet;

	@BeforeAll
	static void setUpFacets() {
		Table employees = Tables.builder("Employee", "E")
				.column(EmployeeDescriptor.ID)
				.column("FIRST_NAME")
				.column("ACTIVE_FLAG")
				.build();

		facets = TableFacets.build(Map.of(EmployeeDescriptor.class, employees));
		employeeFacet = facets.facetFor(EmployeeDescriptor.class);
	}

	@Test
	void facetLookupIsCaseInsensitive() {
		assertThat(employeeFacet.column("ID")).isSameAs(EmployeeDescriptor.ID);
		assertThat(employeeFacet.column("firstName").column().getName()).isEqualTo("FIRST_NAME");
		assertThat(employeeFacet.column("ACTIVE")).isSameAs(employeeFacet.column("active"));
	}

	@Test
	void generatedViewTypeIsPreferredWhenPresent() {
		EmployeeColumns view = facets.columns(EmployeeDescriptor.class, EmployeeColumns.class);
		assertThat(view).isInstanceOf(EmployeeColumnsImpl.class);
		assertThat(view.ID()).isSameAs(EmployeeDescriptor.ID);
		assertThat(view.FIRST_NAME().column().getName()).isEqualTo("FIRST_NAME");
	}

	@Test
	void proxyViewIsUsedWhenNoImplementationExists() {
		DynamicColumns view = facets.columns(EmployeeDescriptor.class, DynamicColumns.class);
		assertThat(Proxy.isProxyClass(view.getClass())).isTrue();
		assertThat(view.ID()).isSameAs(EmployeeDescriptor.ID);
	}

	@Test
	void proxyRejectsMethodsWithArguments() {
		InvalidColumns view = facets.columns(EmployeeDescriptor.class, InvalidColumns.class);
		assertThatThrownBy(() -> view.ID("ignored"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("must have no arguments");
	}



	@SuppressWarnings("unchecked")
	@Test
	void tableRowBuilderKeepsTypeSafety() {
		TableRow row = employeeFacet.rowBuilder()
				.set(EmployeeDescriptor.ID, 42L)
				.set(column("FIRST_NAME", String.class), "Ada")
				.build();

		assertThat(row.get(EmployeeDescriptor.ID)).isEqualTo(42L);
		assertThat(row.asMap()).hasSize(2);

		@SuppressWarnings({ "rawtypes" })
		ColumnRef rawId = (ColumnRef) employeeFacet.column("ID");
		assertThatThrownBy(() -> employeeFacet.rowBuilder().set(rawId, "bad"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void columnRefMustBeBoundBeforeUse() {
		ColumnRef<String> orphan = ColumnRef.of("UNKNOWN", String.class);
		assertThatThrownBy(orphan::column)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Column not bound");
	}

	@Test
	void facetColumnsExposeRowBuilderShortcut() {
		TableRow.Builder builder = employeeFacet.rowBuilder();
		builder.set(EmployeeDescriptor.ID, 7L);
		TableRow row = builder.build();
		assertThat(row.asMap()).containsKey(EmployeeDescriptor.ID);
	}

	interface DynamicColumns {
		ColumnRef<Long> ID();
	}

	interface InvalidColumns {
		ColumnRef<Long> ID(String unused);
	}

	interface EmployeeColumns {
		ColumnRef<Long> ID();

		ColumnRef<String> FIRST_NAME();
	}

	private static final class EmployeeColumnsImpl implements EmployeeColumns {
		private final Facet facet;
		private ColumnRef<Long> cachedId;
		private ColumnRef<String> cachedFirstName;

		private EmployeeColumnsImpl(Facet facet) {
			this.facet = facet;
		}

		@Override
		public ColumnRef<Long> ID() {
			if (cachedId == null) {
				cachedId = cast(facet.column("ID"));
			}
			return cachedId;
		}

		@Override
		public ColumnRef<String> FIRST_NAME() {
			if (cachedFirstName == null) {
				cachedFirstName = cast(facet.column("FIRST_NAME"));
			}
			return cachedFirstName;
		}

		@SuppressWarnings("unchecked")
		private static <T> ColumnRef<T> cast(ColumnRef<?> ref) {
			return (ColumnRef<T>) ref;
		}
	}

	private static <T> ColumnRef<T> column(String name, Class<T> type) {
		ColumnRef<?> ref = employeeFacet.column(name);
		assertThat(type.isAssignableFrom(ref.type())).isTrue();
		@SuppressWarnings("unchecked")
		ColumnRef<T> typed = (ColumnRef<T>) ref;
		return typed;
	}

	private static final class EmployeeDescriptor {
		private EmployeeDescriptor() {
		}

		@SqlColumn(name = "ID", alias = "employeeId")
		static ColumnRef<Long> ID = ColumnRef.of("ID", Long.class);

		@SqlColumn(name = "FIRST_NAME", alias = "firstName")
		static String FIRST_NAME;

		@SqlColumn(name = "ACTIVE_FLAG", alias = "active", javaType = Boolean.class)
		static boolean active;
	}
}
