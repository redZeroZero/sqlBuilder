package org.in.media.res.sqlBuilder.integration.scenario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.in.media.res.sqlBuilder.api.query.CompiledQuery;
import org.in.media.res.sqlBuilder.api.query.SqlAndParams;

public final class ScenarioSupport {

	private ScenarioSupport() {
	}

	public static void executeQuery(Connection connection, SqlAndParams andParams, String title) throws SQLException {
		System.out.printf("\n== %s ==%n", title);
		System.out.println(andParams.sql());
		try (PreparedStatement stmt = connection.prepareStatement(andParams.sql())) {
			bindParams(stmt, andParams.params());
			try (ResultSet result = stmt.executeQuery()) {
				printResultSet(result);
			}
		}
	}

	public static void executeCompiledQuery(Connection connection, CompiledQuery compiledQuery, Map<String, ?> params,
			String title) throws SQLException {
		SqlAndParams bound = compiledQuery.bind(params);
		executeQuery(connection, bound, title);
	}

	public static int executeUpdate(Connection connection, SqlAndParams andParams, String title) throws SQLException {
		System.out.printf("\n== %s ==%n", title);
		System.out.println(andParams.sql());
		try (PreparedStatement stmt = connection.prepareStatement(andParams.sql())) {
			bindParams(stmt, andParams.params());
			int updated = stmt.executeUpdate();
			System.out.println("Rows affected: " + updated);
			return updated;
		}
	}

	public static SqlAndParams bindCompiled(CompiledQuery compiledQuery, Map<String, ?> params) {
		return compiledQuery.bind(params);
	}

	private static void bindParams(PreparedStatement stmt, List<Object> params) throws SQLException {
		for (int i = 0; i < params.size(); i++) {
			stmt.setObject(i + 1, params.get(i));
		}
	}

	private static void printResultSet(ResultSet result) throws SQLException {
		ResultSetMetaData meta = result.getMetaData();
		while (result.next()) {
			StringBuilder row = new StringBuilder();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				if (i > 1) {
					row.append(" | ");
				}
				row.append(meta.getColumnLabel(i)).append("=").append(result.getObject(i));
			}
			System.out.println(row);
		}
	}
}
