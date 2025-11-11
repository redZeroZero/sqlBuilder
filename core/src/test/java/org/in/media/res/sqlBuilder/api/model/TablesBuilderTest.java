package org.in.media.res.sqlBuilder.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TablesBuilderTest {

    @Test
    void columnsAreRegisteredAndBound() {
        ColumnRef<Long> id = ColumnRef.of("ID", Long.class);
        Table table = Tables.builder("Employee", "E")
                .column(id)
                .column("FIRST_NAME", "firstName")
                .build();

        assertThat(table.get("ID")).isSameAs(id.column());
        assertThat(id.column().transpile(false)).isEqualTo("\"E\".\"ID\"");
        assertThat(table.get("FIRST_NAME").transpile()).contains("FIRST_NAME");
    }

    @Test
    void builderRequiresAtLeastOneColumn() {
        assertThrows(IllegalStateException.class, () -> Tables.builder("T", "A").build());
    }
}
