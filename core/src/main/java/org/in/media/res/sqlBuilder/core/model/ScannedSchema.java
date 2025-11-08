package org.in.media.res.sqlBuilder.core.model;

import java.util.List;
import java.util.Objects;

import org.in.media.res.sqlBuilder.api.model.Schema;
import org.in.media.res.sqlBuilder.api.model.Table;
import org.in.media.res.sqlBuilder.api.query.Dialect;
import org.in.media.res.sqlBuilder.core.query.dialect.Dialects;

public class ScannedSchema implements Schema {

	private static final SchemaCache CACHE = new SchemaCache();

	private final String basePackage;
	private List<Table> tables;
	private String schemaName;
	private volatile TableFacets facets;
    private volatile Dialect dialect = Dialects.defaultDialect();

	public ScannedSchema(String basePackage) {
		this.basePackage = Objects.requireNonNull(basePackage, "basePackage");
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		this.tables = CACHE.lookup(basePackage, loader);
		this.facets = SchemaScanner.facets(basePackage, loader);
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
		TableFacets.Facet facet = facets().all().get(clazz);
		if (facet != null) {
			return facet.table();
		}
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

	@Override
	public Dialect getDialect() {
		Dialect current = dialect;
		return current == null ? Dialects.defaultDialect() : current;
	}

	@Override
	public void setDialect(Dialect dialect) {
		this.dialect = Objects.requireNonNull(dialect, "dialect");
	}

	public void refresh() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		SchemaScanner.invalidate(basePackage, loader);
		this.tables = CACHE.refresh(basePackage, loader);
		this.facets = SchemaScanner.facets(basePackage, loader);
	}

	public String getBasePackage() {
		return basePackage;
	}

	public TableFacets facets() {
		TableFacets snapshot = facets;
		if (snapshot == null) {
			synchronized (this) {
				if (facets == null) {
					facets = SchemaScanner.facets(basePackage, Thread.currentThread().getContextClassLoader());
				}
				snapshot = facets;
			}
		}
		return snapshot;
	}

	public TableFacets.Facet facet(Class<?> descriptorClass) {
		return facets().facetFor(descriptorClass);
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
