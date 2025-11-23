package org.in.media.res.sqlBuilder.integration.boot.query;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;

@Component
public class QueryCatalog {

	private final List<QueryDefinition> definitions;
	private final Map<String, QueryDefinition> byId;

	public QueryCatalog(List<QueryDefinition> definitions) {
		this.definitions = definitions.stream()
				.sorted(Comparator.comparing(QueryDefinition::id))
				.toList();
		Map<String, QueryDefinition> mutable = new LinkedHashMap<>();
		for (QueryDefinition definition : this.definitions) {
			mutable.put(definition.id(), definition);
		}
		this.byId = Map.copyOf(mutable);
	}

	public List<QueryDefinition> definitions() {
		return definitions;
	}

	public QueryDefinition definition(String id) {
		return byId.get(id);
	}
}
