package org.in.media.res.sqlBuilder.integration.boot.query;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.in.media.res.sqlBuilder.api.query.SqlAndParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public final class QueryDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryDefinition.class);

	private final String id;
	private final String title;
	private final String description;
	private final Supplier<SqlAndParams> sqlSupplier;

	public QueryDefinition(String id, String title, String description, Supplier<SqlAndParams> sqlSupplier) {
		this.id = Objects.requireNonNull(id, "id");
		this.title = Objects.requireNonNull(title, "title");
		this.description = Objects.requireNonNull(description, "description");
		this.sqlSupplier = Objects.requireNonNull(sqlSupplier, "sqlSupplier");
	}

	public String id() {
		return id;
	}

	public String title() {
		return title;
	}

	public String description() {
		return description;
	}

	public QuerySummary summary() {
		return new QuerySummary(id, title, description);
	}

	public QueryExecution execute(JdbcTemplate jdbcTemplate) {
		SqlAndParams sqlAndParams = sqlSupplier.get();
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sqlAndParams.sql(),
				sqlAndParams.params().toArray());
		LOGGER.info("Executing {} -> {}", id, sqlAndParams.sql());
		if (!sqlAndParams.params().isEmpty()) {
			LOGGER.info("Params: {}", sqlAndParams.params());
		}
		return new QueryExecution(id, title, description, sqlAndParams.sql(),
				List.copyOf(sqlAndParams.params()), rows);
	}
}
