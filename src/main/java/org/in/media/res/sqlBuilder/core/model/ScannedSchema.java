package org.in.media.res.sqlBuilder.core.model;

import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Schema;
import org.in.media.res.sqlBuilder.api.model.Table;

public class ScannedSchema implements Schema {

	private static final SchemaCache CACHE = new SchemaCache();

	private final String basePackage;
	private List<Table> tables;
	private String schemaName;

	public ScannedSchema(String basePackage) {
		this.basePackage = Objects.requireNonNull(basePackage, "basePackage");
		this.tables = CACHE.lookup(basePackage, Thread.currentThread().getContextClassLoader());
	}

	@Override
	public String getName() {
		return schemaName;
	}

	@Override
	public void setName(String name) {
		this.schemaName = name;
		for (Table table : tables) {
			table.includeSchema(name);
		}
	}

	@Override
	public List<Table> getTables() {
		return tables;
	}

	@Override
	public Table getTableBy(Class<?> clazz) {
		return getTableBy(clazz.getSimpleName());
	}

	@Override
	public Table getTableBy(String name) {
		for (Table table : tables) {
			if (table.getName().equals(name)) {
				return table;
			}
		}
		return null;
	}

	public void refresh() {
		this.tables = CACHE.refresh(basePackage, Thread.currentThread().getContextClassLoader());
	}

	public String getBasePackage() {
		return basePackage;
	}

	public static void clearCache() {
		CACHE.clear();
	}

	private static final class SchemaCache {
		private final java.util.Map<CacheKey, List<Table>> cache = new java.util.concurrent.ConcurrentHashMap<>();

		List<Table> lookup(String basePackage, ClassLoader classLoader) {
			CacheKey key = new CacheKey(basePackage, classLoader);
			return cache.computeIfAbsent(key, k -> List.copyOf(SchemaScanner.scan(basePackage, classLoader)));
		}

		List<Table> refresh(String basePackage, ClassLoader classLoader) {
			CacheKey key = new CacheKey(basePackage, classLoader);
			List<Table> tables = List.copyOf(SchemaScanner.scan(basePackage, classLoader));
			cache.put(key, tables);
			return tables;
		}

		void clear() {
			cache.clear();
		}

		private record CacheKey(String basePackage, ClassLoader classLoader) {
		}
	}
}
