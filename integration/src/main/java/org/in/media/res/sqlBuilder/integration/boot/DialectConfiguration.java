package org.in.media.res.sqlBuilder.integration.boot;

import org.in.media.res.sqlBuilder.api.query.Dialects;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
class DialectConfiguration {

	@Value("${sqlbuilder.integration.dialect:postgres}")
	private String dialectName;

	@PostConstruct
	void configureDialect() {
		if ("oracle".equalsIgnoreCase(dialectName)) {
			IntegrationSchema.schema().setDialect(Dialects.oracle());
		} else {
			IntegrationSchema.schema().setDialect(Dialects.postgres());
		}
	}
}
