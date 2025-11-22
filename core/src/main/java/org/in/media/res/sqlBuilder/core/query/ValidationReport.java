package org.in.media.res.sqlBuilder.core.query;

import java.util.ArrayList;
import java.util.List;

public final class ValidationReport {

	private final List<ValidationMessage> messages = new ArrayList<>();

	public void add(ValidationMessage.Severity severity, String code, String message) {
		messages.add(new ValidationMessage(severity, code, message));
	}

	public List<ValidationMessage> messages() {
		return List.copyOf(messages);
	}

	public boolean hasErrors() {
		return messages.stream().anyMatch(m -> m.severity() == ValidationMessage.Severity.ERROR);
	}

	public static ValidationReport empty() {
		return new ValidationReport();
	}
}
