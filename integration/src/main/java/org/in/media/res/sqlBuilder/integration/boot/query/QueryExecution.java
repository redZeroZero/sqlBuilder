package org.in.media.res.sqlBuilder.integration.boot.query;

import java.util.List;
import java.util.Map;

public record QueryExecution(String id, String title, String description, String sql, List<Object> params,
		List<Map<String, Object>> rows) {
}
