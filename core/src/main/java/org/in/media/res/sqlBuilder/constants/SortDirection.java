package org.in.media.res.sqlBuilder.constants;

public enum SortDirection {

	ASC("ASC"), DESC("DESC");

	private final String keyword;

	SortDirection(String keyword) {
		this.keyword = keyword;
	}

	public String value() {
		return keyword;
	}

}
