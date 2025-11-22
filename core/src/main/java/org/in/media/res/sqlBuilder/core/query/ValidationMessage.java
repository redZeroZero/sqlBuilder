package org.in.media.res.sqlBuilder.core.query;

public final class ValidationMessage {

	public enum Severity {
		ERROR, WARNING, INFO
	}

	private final Severity severity;
	private final String code;
	private final String message;

	public ValidationMessage(Severity severity, String code, String message) {
		this.severity = severity;
		this.code = code;
		this.message = message;
	}

	public Severity severity() {
		return severity;
	}

	public String code() {
		return code;
	}

	public String message() {
		return message;
	}
}
