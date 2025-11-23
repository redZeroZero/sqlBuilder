package org.in.media.res.sqlBuilder.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.api.query.Dialects;
import org.in.media.res.sqlBuilder.integration.model.IntegrationSchema;

public final class IntegrationConfig {

	private final String jdbcUrl;
	private final String user;
	private final String password;
	private final Dialect dialect;

	private IntegrationConfig(String jdbcUrl, String user, String password, Dialect dialect) {
		this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl");
		this.user = Objects.requireNonNull(user, "user");
		this.password = Objects.requireNonNull(password, "password");
		this.dialect = Objects.requireNonNull(dialect, "dialect");
	}

	public static IntegrationConfig fromEnvironment() {
		String url = System.getenv().getOrDefault("SQLBUILDER_JDBC_URL", "jdbc:postgresql://localhost:5432/sqlbuilder");
		String user = System.getenv().getOrDefault("SQLBUILDER_JDBC_USER", "sb_user");
		String password = System.getenv().getOrDefault("SQLBUILDER_JDBC_PASSWORD", "sb_pass");
		String dialectName = System.getenv().getOrDefault("SQLBUILDER_DIALECT", "postgres");
		Dialect dialect = parseDialect(dialectName);
		IntegrationSchema.schema().setDialect(dialect);
		return new IntegrationConfig(url, user, password, dialect);
	}

	public Connection connect() throws SQLException {
		return DriverManager.getConnection(jdbcUrl, user, password);
	}

    public String jdbcUrl() {
        return jdbcUrl;
    }

    public String user() {
        return user;
    }

	public String password() {
		return password;
	}

	public Dialect dialect() {
		return dialect;
	}

	private static Dialect parseDialect(String dialectName) {
		if (dialectName == null) {
			return Dialects.postgres();
		}
		return switch (dialectName.toLowerCase()) {
			case "oracle" -> Dialects.oracle();
			case "postgres", "postgresql" -> Dialects.postgres();
			default -> Dialects.postgres();
		};
	}
}
