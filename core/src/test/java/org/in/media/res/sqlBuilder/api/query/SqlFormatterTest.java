package org.in.media.res.sqlBuilder.api.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.List;

import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;
import org.junit.jupiter.api.Test;

class SqlFormatterTest {

	@Test
	void inlineLiteralsFormatsNumbersStringsDatesAndNulls() {
		Date timestamp = new Date(1_700_000_000_000L); // deterministic instant
		SqlAndParams sql = new SqlAndParams("SELECT ?, ?, ?, ?",
				java.util.Arrays.asList("O'Reilly", 42, timestamp, null));

		String rendered = SqlFormatter.inlineLiterals(sql, Dialects.oracle());

		assertThat(rendered).contains("'O''Reilly'");
		assertThat(rendered).contains("42");
		assertThat(rendered).contains("'2023-11-");
		assertThat(rendered).endsWith("NULL");
	}

	@Test
	void inlineLiteralsRejectsUnboundParameters() {
		SqlParameter<String> param = SqlParameters.param("name");
		SqlAndParams sql = new SqlAndParams("SELECT ?", List.of(param));

		assertThatThrownBy(() -> SqlFormatter.inlineLiterals(sql, Dialects.postgres()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Unbound parameter 'name'");
	}
}
