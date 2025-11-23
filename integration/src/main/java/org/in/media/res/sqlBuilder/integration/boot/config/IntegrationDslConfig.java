package org.in.media.res.sqlBuilder.integration.boot.config;

import org.in.media.res.sqlBuilder.api.model.ScannedSchema;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.Dialects;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntegrationDslConfig {

	@Bean
	public Dialect integrationDialect(@Value("${sqlbuilder.integration.dialect:postgres}") String dialectName) {
		if ("oracle".equalsIgnoreCase(dialectName)) {
			return Dialects.oracle();
		}
		return Dialects.postgres();
	}

	@Bean
	public ScannedSchema integrationSchema(Dialect integrationDialect) {
		ScannedSchema schema = IntegrationSchema.schema();
		schema.setDialect(integrationDialect);
		return schema;
	}
}
