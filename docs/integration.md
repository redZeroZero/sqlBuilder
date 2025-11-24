# Integration & Modules

- **PostgreSQL integration**: see `integration/` for dialect and examples wired to Postgres.
- **Spring Boot demo API**: sample REST API showing query construction + execution.
- **Spring JDBC integration**: adapters for `JdbcTemplate` using `CompiledQuery` / `SqlAndParams`.

## Integration Module with PostgreSQL

The `integration` module drives sqlBuilder against a PostgreSQL container with a commerce-style schema.

1. Start the DB (`integration/docker/docker-compose.yml`):
   ```bash
   docker compose -f integration/docker/docker-compose.yml up -d
   ```
2. Run the harness (ensure PostgreSQL driver is in your Maven cache):
   ```bash
   mvn -pl integration exec:java
   ```
   `IntegrationApp` reads `SQLBUILDER_JDBC_URL`, `SQLBUILDER_JDBC_USER`, `SQLBUILDER_JDBC_PASSWORD` (defaults to `jdbc:postgresql://localhost:5432/sqlbuilder`, `sb_user`, `sb_pass`).
3. Stop when done:
   ```bash
   docker compose -f integration/docker/docker-compose.yml down
   ```

## Spring Boot demo API

Runs against the same Postgres container and exposes the demo queries over HTTP.

```bash
mvn -pl integration spring-boot:run
```

- Env vars: `SQLBUILDER_JDBC_URL`, `SQLBUILDER_JDBC_USER`, `SQLBUILDER_JDBC_PASSWORD` (server binds to `${PORT:-8080}`).
- Endpoints: `GET /queries` (catalog), `GET /queries/{id}` (SQL, params, rows).

## Spring JDBC integration

`spring-jdbc` module exposes `SqlBuilderJdbcTemplate`, a wrapper around Spring's `JdbcTemplate` that accepts `SqlAndParams` / `CompiledQuery`.

```xml
<dependency>
  <groupId>org.in.media.res</groupId>
  <artifactId>sqlbuilder-spring-jdbc</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Basic wiring:

```java
@Bean
SqlBuilderJdbcTemplate sqlBuilderJdbcTemplate(JdbcTemplate jdbcTemplate) {
    return new SqlBuilderJdbcTemplate(jdbcTemplate);
}
```

Use with compiled queries:

```java
CompiledQuery cq = SqlQuery.newQuery()
    .select(...)
    .from(...)
    .compile();

SqlAndParams sap = cq.bind(Map.of("id", 42));
List<MyDto> rows = sqlBuilderJdbcTemplate.query(sap, (rs, rowNum) -> new MyDto(...));
```

Need to run a fully raw statement? Build `SqlAndParams` directly:

```java
SqlAndParams raw = SqlQuery.raw("SELECT * FROM employees WHERE status = ?", "ACTIVE");
sqlBuilderJdbcTemplate.query(raw, rowMapper);
```

### Debug logging with `SqlFormatter.inlineLiterals`

Render once for logging, keep `SqlAndParams` for JDBC execution:

```java
SqlAndParams sap = query.render();
String debugSql = SqlFormatter.inlineLiterals(sap, dialect);
log.debug("Executing sqlBuilder query: {}", debugSql);
```

### DML helpers via Spring JDBC

`SqlBuilderJdbcTemplate` also works with `UpdateQuery` / `DeleteQuery`:

```java
SqlParameter<Integer> id = SqlParameters.param("id");
SqlParameter<BigDecimal> salary = SqlParameters.param("salary");

UpdateQuery updateSalary = SqlQuery.update(employee)
    .set(Employee.C_SALARY, salary)
    .where(Employee.C_ID).eq(id);

sqlBuilderJdbcTemplate.update(updateSalary, Map.of("salary", new BigDecimal("90000"), "id", 42));
```
