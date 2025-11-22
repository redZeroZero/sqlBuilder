package org.in.media.res.sqlBuilder.integration.boot.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class JdbcConfig {

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource,
			@Value("${sqlbuilder.integration.max-rows:100}") int maxRows) {
		JdbcTemplate template = new JdbcTemplate(dataSource);
		template.setMaxRows(maxRows);
		return template;
	}
}
