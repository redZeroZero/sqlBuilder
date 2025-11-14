package org.in.media.res.sqlBuilder.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.in.media.res.sqlBuilder.core.model.samples.AnnotatedCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScannedSchemaTest {

    private static final String BASE_PACKAGE = "org.in.media.res.sqlBuilder.core.model.samples";

    @BeforeEach
    void resetCache() {
        ScannedSchema.clearCache();
    }

    @Test
    void getTableByDescriptorAndRefresh() {
        ScannedSchema schema = new ScannedSchema(BASE_PACKAGE);
        Table table = schema.getTableBy(AnnotatedCustomer.class);
        assertThat(table.getName()).isEqualTo("Customer");

        schema.refresh();
        assertThat(schema.getTableBy(AnnotatedCustomer.class)).isNotNull();
    }

    @Test
    void fallbackToSimpleNameAndErrorsWhenMissing() {
        ScannedSchema schema = new ScannedSchema(BASE_PACKAGE);
        assertThat(schema.getTableBy("Customer")).isNotNull();
        assertThatThrownBy(() -> schema.getTableBy(String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No table registered");
    }
}
