package org.in.media.res.sqlBuilder.api.query;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CompiledQueryBindingTest {

    @Test
    void bindMapRejectsUnknownKeys() {
        SqlParameter<Integer> id = SqlParameters.param("id", Integer.class);
        CompiledQuery compiled = new CompiledQuery("SELECT 1 WHERE ?", List.of(new CompiledQuery.Placeholder(id, null)));

        assertThatThrownBy(() -> compiled.bind(Map.of("id", 1, "extra", 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown parameter 'extra'");
    }

    @Test
    void bindVarargsDisallowsDuplicates() {
        SqlParameter<Integer> id = SqlParameters.param("id", Integer.class);
        List<CompiledQuery.Placeholder> placeholders = List.of(
                new CompiledQuery.Placeholder(id, null),
                new CompiledQuery.Placeholder(id, null));
        CompiledQuery compiled = new CompiledQuery("SELECT 1 WHERE ? OR ?", placeholders);

        assertThatThrownBy(() -> compiled.bind(1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Varargs binding disallowed");
    }
}
