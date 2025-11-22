package org.in.media.res.sqlBuilder.integration.boot;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.integration.boot.query.QueryController;
import org.in.media.res.sqlBuilder.integration.boot.query.QueryExecution;
import org.in.media.res.sqlBuilder.integration.boot.query.QueryService;
import org.in.media.res.sqlBuilder.integration.boot.query.QuerySummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QueryController.class)
class QueryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private QueryService queryService;

	@Test
	void listsQueries() throws Exception {
		var summary = new QuerySummary("simple-projection", "Simple Projection", "desc");
		given(queryService.listQueries()).willReturn(List.of(summary));

		mockMvc.perform(get("/queries"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("simple-projection"))
				.andExpect(jsonPath("$[0].title").value("Simple Projection"));
	}

	@Test
	void runsQueryById() throws Exception {
		var execution = new QueryExecution("simple-projection", "Simple Projection", "desc", "select 1",
				List.of(), List.of(Map.of("id", 1)));
		given(queryService.run("simple-projection")).willReturn(execution);

		mockMvc.perform(get("/queries/simple-projection"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sql").value("select 1"))
				.andExpect(jsonPath("$.rows[0].id").value(1));
	}
}
