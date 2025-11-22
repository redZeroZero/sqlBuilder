package org.in.media.res.sqlBuilder.integration.boot.query;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/queries")
public class QueryController {

	private final QueryService queryService;

	public QueryController(QueryService queryService) {
		this.queryService = queryService;
	}

	@GetMapping
	public List<QuerySummary> list() {
		return queryService.listQueries();
	}

	@GetMapping("/{id}")
	public QueryExecution run(@PathVariable String id) {
		return queryService.run(id);
	}
}
