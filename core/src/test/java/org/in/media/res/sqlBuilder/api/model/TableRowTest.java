package org.in.media.res.sqlBuilder.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TableRowTest {

    @Test
    void builderRejectsIncompatibleValues() {
        ColumnRef raw = ColumnRef.of("ID", Long.class);
        assertThatThrownBy(() -> TableRow.builder().set(raw, "bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void asMapExposesImmutableValues() {
        ColumnRef<String> name = ColumnRef.of("NAME", String.class);
        TableRow row = TableRow.builder().set(name, "Alice").build();
        assertThat(row.asMap()).containsEntry(name, "Alice");
    }
}
