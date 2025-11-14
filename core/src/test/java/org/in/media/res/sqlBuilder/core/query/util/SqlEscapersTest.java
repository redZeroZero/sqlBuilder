package org.in.media.res.sqlBuilder.core.query.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SqlEscapersTest {

	@Test
	void escapeStringLiteralDoublesSingleQuotes() {
		assertThat(SqlEscapers.escapeStringLiteral("O'Brien"))
				.isEqualTo("O''Brien");
	}

	@Test
	void escapeStringLiteralPreservesNull() {
		assertThat(SqlEscapers.escapeStringLiteral(null)).isNull();
	}

	@Test
	void escapeLikePatternEscapesWildcardsWithDefaultChar() {
		assertThat(SqlEscapers.escapeLikePattern("50%_complete\\path"))
				.isEqualTo("50\\%\\_complete\\\\path");
	}

	@Test
	void escapeLikePatternSupportsCustomEscapeCharacter() {
		assertThat(SqlEscapers.escapeLikePattern("value_100%", '!'))
				.isEqualTo("value!_100!%");
	}
}
