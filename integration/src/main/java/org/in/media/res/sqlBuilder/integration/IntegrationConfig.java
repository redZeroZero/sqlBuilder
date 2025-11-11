package org.in.media.res.sqlBuilder.integration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public final class IntegrationConfig {

    private final String jdbcUrl;
    private final String user;
    private final String password;

    private IntegrationConfig(String jdbcUrl, String user, String password) {
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl");
        this.user = Objects.requireNonNull(user, "user");
        this.password = Objects.requireNonNull(password, "password");
    }

    public static IntegrationConfig fromEnvironment() {
        String url = System.getenv().getOrDefault("SQLBUILDER_JDBC_URL", "jdbc:postgresql://localhost:5432/sqlbuilder");
        String user = System.getenv().getOrDefault("SQLBUILDER_JDBC_USER", "sb_user");
        String password = System.getenv().getOrDefault("SQLBUILDER_JDBC_PASSWORD", "sb_pass");
        return new IntegrationConfig(url, user, password);
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
}
