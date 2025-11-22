package org.in.media.res.sqlBuilder.integration.boot.query;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class QueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

	private final QueryCatalog catalog;
	private final JdbcTemplate jdbcTemplate;

	public QueryService(QueryCatalog catalog, JdbcTemplate jdbcTemplate) {
		this.catalog = catalog;
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<QuerySummary> listQueries() {
		return catalog.definitions()
				.stream()
				.map(QueryDefinition::summary)
				.toList();
	}

	@Transactional(readOnly = true)
	public QueryExecution run(String id) {
		QueryDefinition definition = catalog.definition(id);
		if (definition == null) {
			throw new QueryNotFoundException(id);
		}
		LOGGER.info("Running integration query: {}", id);
		return definition.execute(jdbcTemplate);
	}
}
