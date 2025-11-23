package org.in.media.res.sqlBuilder.api.query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/** Utility that renders {@link SqlAndParams} with inline literal values. */
public final class SqlFormatter {

    private SqlFormatter() {
    }

    public static String inlineLiterals(SqlAndParams sqlAndParams, Dialect dialect) {
        String sql = sqlAndParams.sql();
        StringBuilder builder = new StringBuilder(sql.length() + 32 * sqlAndParams.params().size());
        Iterator<Object> params = sqlAndParams.params().iterator();
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '?' && params.hasNext()) {
                builder.append(formatLiteral(params.next(), dialect));
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    /** Options controlling {@link #prettyPrint(String, PrettyPrintOptions)}. */
    public static final class PrettyPrintOptions {

        public enum IndentStyle {
            TABS, SPACES
        }

        private final int indentWidth;
        private final IndentStyle indentStyle;

        private PrettyPrintOptions(int indentWidth, IndentStyle indentStyle) {
            this.indentWidth = indentWidth;
            this.indentStyle = indentStyle;
        }

        public static PrettyPrintOptions tabs(int tabCount) {
            return new PrettyPrintOptions(Math.max(tabCount, 1), IndentStyle.TABS);
        }

        public static PrettyPrintOptions spaces(int width) {
            return new PrettyPrintOptions(Math.max(width, 1), IndentStyle.SPACES);
        }

        static PrettyPrintOptions defaultOptions() {
            return spaces(2);
        }

        String indentString(int level) {
            String unit = indentStyle == IndentStyle.TABS ? "\t" : " ".repeat(indentWidth);
            return unit.repeat(Math.max(level, 0));
        }
    }

	/** Pretty print SQL by inserting new lines/indentation around common clauses. */
	public static String prettyPrint(String sql) {
		return prettyPrint(sql, PrettyPrintOptions.defaultOptions());
	}

	public static String prettyPrint(String sql, PrettyPrintOptions options) {
		if (sql == null || sql.isBlank()) {
			return sql;
		}
		PrettyPrintOptions opts = options == null ? PrettyPrintOptions.defaultOptions() : options;
		WithBlock withBlock = parseWithBlock(sql);
		if (withBlock != null) {
			return formatWithBlock(withBlock, opts);
		}
		String clausesBroken = breakIntoClauses(sql, opts);
		return formatClauses(clausesBroken, opts);
	}

    private static boolean isClauseBoundary(String upperSql, int index) {
        if (index > 0 && Character.isLetterOrDigit(upperSql.charAt(index - 1))) {
            return false;
        }
        int end = index;
        while (end < upperSql.length() && Character.isLetter(upperSql.charAt(end))) {
            end++;
        }
        return end == upperSql.length() || Character.isWhitespace(upperSql.charAt(end))
                || upperSql.charAt(end) == '(';
    }

	private static int nextKeywordLength(String upperSql, int index) {
		int end = index;
		while (end < upperSql.length() && Character.isLetter(upperSql.charAt(end))) {
			end++;
		}
		return end - index;
	}

	private static String formatClauses(String rawSql, PrettyPrintOptions opts) {
		String normalized = rawSql.replace("\r", "");
		String[] lines = normalized.stripLeading().split("\n");
		StringBuilder builder = new StringBuilder(normalized.length() + 32);
		for (String line : lines) {
			if (line.isBlank()) {
				continue;
			}
			if (builder.length() > 0) {
				builder.append('\n');
			}
			builder.append(formatClauseLine(line, opts));
		}
		return builder.toString().stripTrailing();
	}

	private static String formatClauseLine(String line, PrettyPrintOptions opts) {
		String trimmedTrailing = line.stripTrailing();
		String withoutLeading = trimmedTrailing.stripLeading();
		int leadingLength = trimmedTrailing.length() - withoutLeading.length();
		String leading = trimmedTrailing.substring(0, leadingLength);
		String upper = withoutLeading.toUpperCase(Locale.ROOT);
		if (upper.startsWith("SELECT")) {
			String rest = withoutLeading.substring("SELECT".length()).trim();
			return formatSelectClause(leading, rest, opts);
		}
		if (upper.startsWith("FROM")) {
			String rest = withoutLeading.substring("FROM".length()).trim();
			return formatFromClause(leading, rest, opts);
		}
		if (upper.startsWith("WHERE")) {
			String rest = withoutLeading.substring("WHERE".length()).trim();
			return formatPredicateClause(leading, "WHERE", rest, opts);
		}
		if (upper.startsWith("HAVING")) {
			String rest = withoutLeading.substring("HAVING".length()).trim();
			return formatPredicateClause(leading, "HAVING", rest, opts);
		}
		if (upper.startsWith("GROUP BY")) {
			String rest = withoutLeading.substring("GROUP BY".length()).trim();
			return formatGroupedClause(leading, "GROUP BY", rest, opts);
		}
		if (upper.startsWith("ORDER BY")) {
			String rest = withoutLeading.substring("ORDER BY".length()).trim();
			return formatGroupedClause(leading, "ORDER BY", rest, opts);
		}
		if (upper.startsWith("WITH")) {
			String rest = withoutLeading.substring("WITH".length()).trim();
			return formatWithClause(leading, rest, opts);
		}
		return leading + withoutLeading;
	}

	private static String formatSelectClause(String leading, String rest, PrettyPrintOptions opts) {
		StringBuilder clause = new StringBuilder();
		clause.append(leading).append("SELECT");
		ClauseHeader header = extractSelectHeader(rest);
		if (!header.extras().isBlank()) {
			clause.append(' ').append(header.extras().trim());
		}
		if (header.body().isBlank()) {
			return clause.toString();
		}
		clause.append('\n');
		appendItems(clause, splitTopLevel(header.body(), ','), leading + opts.indentString(1), true);
		return clause.toString();
	}

	private static String formatFromClause(String leading, String rest, PrettyPrintOptions opts) {
		StringBuilder clause = new StringBuilder();
		clause.append(leading).append("FROM");
		if (rest.isBlank()) {
			return clause.toString();
		}
		List<String> sources = splitFromSources(rest);
		if (sources.isEmpty()) {
			return clause.append(' ').append(rest).toString();
		}
		clause.append('\n');
		appendFromItems(clause, sources, leading + opts.indentString(1), opts);
		return clause.toString();
	}

	private static String formatPredicateClause(String leading, String keyword, String rest,
			PrettyPrintOptions opts) {
		StringBuilder clause = new StringBuilder();
		clause.append(leading).append(keyword);
		if (rest.isBlank()) {
			return clause.toString();
		}
		List<PredicateExpression> expressions = splitLogicalExpressions(rest);
		if (expressions.isEmpty()) {
			return clause.toString();
		}
		clause.append('\n');
		appendPredicateItems(clause, expressions, leading + opts.indentString(1), opts);
		return clause.toString();
	}

	private static String formatGroupedClause(String leading, String keyword, String rest, PrettyPrintOptions opts) {
		StringBuilder clause = new StringBuilder();
		clause.append(leading).append(keyword);
		if (rest.isBlank()) {
			return clause.toString();
		}
		clause.append('\n');
		appendItems(clause, splitTopLevel(rest, ','), leading + opts.indentString(1), true);
		return clause.toString();
	}

	private static String formatWithClause(String leading, String rest, PrettyPrintOptions opts) {
		StringBuilder clause = new StringBuilder(rest.length() + 32);
		clause.append(leading).append("WITH");
		if (rest.isBlank()) {
			return clause.toString();
		}
		clause.append('\n');
		List<String> ctes = splitTopLevel(rest, ',');
		String cteIndent = leading + opts.indentString(1);
		for (int i = 0; i < ctes.size(); i++) {
			formatCte(clause, ctes.get(i), cteIndent, opts);
			if (i < ctes.size() - 1) {
				clause.append(",\n");
			}
		}
		return clause.toString();
	}

	private static void formatCte(StringBuilder target, String rawCte, String indent, PrettyPrintOptions opts) {
		String trimmed = rawCte.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		int asIndex = findKeywordOutsideParens(trimmed, "AS");
		if (asIndex <= 0 || asIndex + 2 >= trimmed.length()) {
			target.append(indent).append(trimmed);
			return;
		}
		String name = trimmed.substring(0, asIndex).trim();
		String body = trimmed.substring(asIndex + 2).trim();
		target.append(indent).append(name).append(" AS");
		if (body.startsWith("(") && body.endsWith(")")) {
			String inner = body.substring(1, body.length() - 1).trim();
			String innerIndent = indent + opts.indentString(1);
			String formattedInner = prettyPrint(inner, opts);
			target.append(" (\n");
			String[] lines = formattedInner.split("\n");
			for (int i = 0; i < lines.length; i++) {
				target.append(innerIndent).append(lines[i]);
				if (i < lines.length - 1) {
					target.append('\n');
				}
			}
			target.append('\n')
					.append(indent)
					.append(')');
			return;
		}
		target.append(' ').append(body);
	}

	private static WithBlock parseWithBlock(String sql) {
		String trimmed = sql.stripLeading();
		if (!startsWithIgnoreCase(trimmed, "WITH")) {
			return null;
		}
		int position = skipWhitespace(trimmed, "WITH".length());
		List<Cte> ctes = new ArrayList<>();
		while (position < trimmed.length()) {
			int asIndex = findKeywordOutsideParens(trimmed, "AS", position);
			if (asIndex < 0) {
				return null;
			}
			String name = trimmed.substring(position, asIndex).trim();
			int bodyStart = skipWhitespace(trimmed, asIndex + 2);
			if (bodyStart >= trimmed.length() || trimmed.charAt(bodyStart) != '(') {
				return null;
			}
			int bodyEnd = findMatchingParenSafe(trimmed, bodyStart);
			if (bodyEnd < 0) {
				return null;
			}
			String body = trimmed.substring(bodyStart + 1, bodyEnd).trim();
			ctes.add(new Cte(name, body));
			position = skipWhitespace(trimmed, bodyEnd + 1);
			if (position < trimmed.length() && trimmed.charAt(position) == ',') {
				position = skipWhitespace(trimmed, position + 1);
				continue;
			}
			break;
		}
		String mainSql = trimmed.substring(position).trim();
		if (mainSql.isEmpty()) {
			return null;
		}
		return new WithBlock(ctes, mainSql);
	}

	private static String formatWithBlock(WithBlock block, PrettyPrintOptions opts) {
		StringBuilder builder = new StringBuilder(block.mainSql().length() + 64);
		builder.append("WITH").append('\n');
		String baseIndent = opts.indentString(1);
		for (int i = 0; i < block.ctes().size(); i++) {
			Cte cte = block.ctes().get(i);
			String innerIndent = baseIndent + opts.indentString(1);
			builder.append(baseIndent).append(cte.name().trim()).append(" AS (\n");
			String formattedInner = prettyPrint(cte.body(), opts);
			String[] lines = formattedInner.split("\n");
			for (int l = 0; l < lines.length; l++) {
				builder.append(innerIndent).append(lines[l]);
				if (l < lines.length - 1) {
					builder.append('\n');
				}
			}
			builder.append('\n').append(baseIndent).append(')');
			if (i < block.ctes().size() - 1) {
				builder.append(',').append('\n');
			} else {
				builder.append('\n');
			}
		}
		builder.append(prettyPrint(block.mainSql(), opts));
		return builder.toString().stripTrailing();
	}

	private static int findMatchingParenSafe(String text, int openIndex) {
		int depth = 0;
		boolean inSingle = false;
		boolean inDouble = false;
		for (int i = openIndex; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'' && !inDouble) {
				inSingle = !inSingle;
				continue;
			}
			if (c == '"' && !inSingle) {
				inDouble = !inDouble;
				continue;
			}
			if (inSingle || inDouble) {
				continue;
			}
			if (c == '(') {
				depth++;
			} else if (c == ')') {
				depth--;
				if (depth == 0) {
					return i;
				}
			}
		}
		return -1;
	}

	private static boolean startsWithIgnoreCase(String text, String keyword) {
		if (text.length() < keyword.length()) {
			return false;
		}
		return text.regionMatches(true, 0, keyword, 0, keyword.length());
	}

	private static void appendItems(StringBuilder target, List<String> items, String indent, boolean commaSeparated) {
		for (int i = 0; i < items.size(); i++) {
			String item = items.get(i).trim();
			target.append(indent).append(item);
			if (commaSeparated && i < items.size() - 1) {
				target.append(',');
			}
			if (i < items.size() - 1) {
				target.append('\n');
			}
		}
	}

	private static void appendFromItems(StringBuilder target, List<String> items, String indent,
			PrettyPrintOptions opts) {
		for (int i = 0; i < items.size(); i++) {
			String formatted = formatFromItem(items.get(i), indent, opts);
			target.append(formatted);
			if (i < items.size() - 1) {
				target.append('\n');
			}
		}
	}

	private static String formatFromItem(String item, String indent, PrettyPrintOptions opts) {
		String trimmed = item.trim();
		if (trimmed.isEmpty()) {
			return "";
		}
		String upper = trimmed.toUpperCase(Locale.ROOT);
		int joinKeywordLength = matchJoinKeyword(upper, 0, trimmed);
		if (joinKeywordLength > 0) {
			String keyword = trimmed.substring(0, joinKeywordLength).trim();
			String remainder = trimmed.substring(joinKeywordLength).trim();
			int onIndex = findKeywordOutsideParens(remainder, "ON");
			String relation = onIndex >= 0 ? remainder.substring(0, onIndex).trim() : remainder;
			String condition = onIndex >= 0 ? remainder.substring(onIndex).trim() : "";
			StringBuilder formatted = new StringBuilder(trimmed.length() + 16);
			formatted.append(indent).append(keyword);
			String relationIndent = indent + opts.indentString(1);
			if (!relation.isBlank()) {
				if (relation.startsWith("(")) {
					int closing = findMatchingParenSafe(relation, 0);
					String inner = closing >= 0 ? relation.substring(1, closing).trim() : relation.substring(1).trim();
					String alias = closing >= 0 && closing + 1 < relation.length()
							? relation.substring(closing + 1).trim()
							: "";
					String formattedInner = prettyPrint(inner, opts);
					formatted.append(" (\n");
					String[] lines = formattedInner.split("\n");
					for (int i = 0; i < lines.length; i++) {
						formatted.append(relationIndent).append(lines[i]);
						if (i < lines.length - 1) {
							formatted.append('\n');
						}
					}
					formatted.append('\n')
							.append(indent)
							.append(')');
					if (!alias.isBlank()) {
						formatted.append(' ').append(alias);
					}
				} else if (relation.contains("\n")) {
					formatted.append('\n')
							.append(relationIndent)
							.append(indentMultiline(relation, relationIndent));
				} else {
					formatted.append(' ').append(relation);
				}
			}
			if (!condition.isBlank()) {
				String onIndent = relationIndent;
				formatted.append('\n')
						.append(onIndent)
						.append(indentMultiline(condition, onIndent));
			}
			return formatted.toString();
		}
		return indent + trimmed;
	}

	private static void appendPredicateItems(StringBuilder target, List<PredicateExpression> expressions, String indent,
			PrettyPrintOptions opts) {
		for (int i = 0; i < expressions.size(); i++) {
			PredicateExpression expression = expressions.get(i);
			appendPredicateExpression(target, indent, expression.connector(), expression.expression(), opts);
			if (i < expressions.size() - 1) {
				target.append('\n');
			}
		}
	}

	private static void appendPredicateExpression(StringBuilder target, String indent, String connector,
			String expression, PrettyPrintOptions opts) {
		String prefix = connector.isBlank() ? indent : indent + connector + " ";
		String trimmed = expression.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		if (isWrappedInParentheses(trimmed)) {
			String inner = trimmed.substring(1, trimmed.length() - 1).trim();
			String innerIndent = indent + opts.indentString(1);
			target.append(prefix).append('(');
			List<PredicateExpression> nested = splitLogicalExpressions(inner);
			if (nested.size() > 1) {
				target.append('\n');
				appendPredicateItems(target, nested, innerIndent, opts);
				target.append('\n').append(indent).append(')');
			} else if (!inner.isEmpty()) {
				target.append('\n')
						.append(innerIndent)
						.append(indentMultiline(inner, innerIndent))
						.append('\n')
						.append(indent).append(')');
			} else {
				target.append(')');
			}
		} else {
			target.append(prefix).append(trimmed);
		}
	}

	private static String indentMultiline(String text, String indent) {
		return text.replace("\n", "\n" + indent);
	}

	private static ClauseHeader extractSelectHeader(String text) {
		int index = 0;
		String upper = text.toUpperCase(Locale.ROOT);
		StringBuilder extras = new StringBuilder();
		while (index < text.length()) {
			index = skipWhitespace(text, index);
			if (index >= text.length()) {
				break;
			}
			if (upper.startsWith("DISTINCT", index) && isWord(text, index, "DISTINCT".length())) {
				int next = index + "DISTINCT".length();
				appendHeaderToken(extras, text.substring(index, next));
				index = next;
				continue;
			}
			if (upper.startsWith("ALL", index) && isWord(text, index, "ALL".length())) {
				int next = index + "ALL".length();
				appendHeaderToken(extras, text.substring(index, next));
				index = next;
				continue;
			}
			if (text.startsWith("/*", index)) {
				int end = consumeBlockComment(text, index);
				appendHeaderToken(extras, text.substring(index, Math.min(end, text.length())));
				index = end;
				continue;
			}
			break;
		}
		return new ClauseHeader(extras.toString(), text.substring(index).trim());
	}

	private static void appendHeaderToken(StringBuilder builder, String token) {
		if (!token.isBlank()) {
			builder.append(' ').append(token.trim());
		}
	}

	private static List<String> splitTopLevel(String text, char delimiter) {
		List<String> parts = new ArrayList<>();
		if (text == null || text.isBlank()) {
			return parts;
		}
		int depth = 0;
		boolean inSingle = false;
		boolean inDouble = false;
		StringBuilder current = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'' && !inDouble) {
				current.append(c);
				if (i + 1 < text.length() && text.charAt(i + 1) == '\'') {
					current.append(text.charAt(i + 1));
					i++;
					continue;
				}
				inSingle = !inSingle;
				continue;
			}
			if (c == '"' && !inSingle) {
				current.append(c);
				if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
					current.append(text.charAt(i + 1));
					i++;
					continue;
				}
				inDouble = !inDouble;
				continue;
			}
			if (inSingle || inDouble) {
				current.append(c);
				continue;
			}
			if (c == '(') {
				depth++;
				current.append(c);
				continue;
			}
			if (c == ')') {
				depth = Math.max(depth - 1, 0);
				current.append(c);
				continue;
			}
			if (c == delimiter && depth == 0) {
				String value = current.toString().trim();
				if (!value.isEmpty()) {
					parts.add(value);
				}
				current.setLength(0);
				continue;
			}
			current.append(c);
		}
		String tail = current.toString().trim();
		if (!tail.isEmpty()) {
			parts.add(tail);
		}
		return parts;
	}

	private static List<String> splitFromSources(String text) {
		List<String> sources = new ArrayList<>();
		if (text.isBlank()) {
			return sources;
		}
		String upper = text.toUpperCase(Locale.ROOT);
		StringBuilder current = new StringBuilder();
		int depth = 0;
		boolean inSingle = false;
		boolean inDouble = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'' && !inDouble) {
				current.append(c);
				if (i + 1 < text.length() && text.charAt(i + 1) == '\'') {
					current.append(text.charAt(i + 1));
					i++;
				} else {
					inSingle = !inSingle;
				}
				continue;
			}
			if (c == '"' && !inSingle) {
				current.append(c);
				if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
					current.append(text.charAt(i + 1));
					i++;
				} else {
					inDouble = !inDouble;
				}
				continue;
			}
			if (inSingle || inDouble) {
				current.append(c);
				continue;
			}
			if (c == '(') {
				depth++;
				current.append(c);
				continue;
			}
			if (c == ')') {
				depth = Math.max(depth - 1, 0);
				current.append(c);
				continue;
			}
			if (depth == 0) {
				int joinLen = matchJoinKeyword(upper, i, text);
				if (joinLen > 0) {
					String base = current.toString().trim();
					if (!base.isEmpty()) {
						sources.add(base);
					}
					current.setLength(0);
					current.append(text, i, i + joinLen).append(' ');
					i += joinLen - 1;
					while (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
						i++;
					}
					continue;
				}
			}
			current.append(c);
		}
		String tail = current.toString().trim();
		if (!tail.isEmpty()) {
			sources.add(tail);
		}
		return sources;
	}

	private static int matchJoinKeyword(String upper, int index, String original) {
		String[][] patterns = {
				{ "LEFT", "OUTER", "JOIN" },
				{ "LEFT", "JOIN" },
				{ "RIGHT", "OUTER", "JOIN" },
				{ "RIGHT", "JOIN" },
				{ "FULL", "OUTER", "JOIN" },
				{ "FULL", "JOIN" },
				{ "INNER", "JOIN" },
				{ "CROSS", "JOIN" },
				{ "JOIN" }
		};
		for (String[] pattern : patterns) {
			int length = matchCompositeKeyword(upper, original, index, pattern);
			if (length > 0) {
				return length;
			}
		}
		return -1;
	}

	private static List<PredicateExpression> splitLogicalExpressions(String text) {
		List<PredicateExpression> expressions = new ArrayList<>();
		if (text.isBlank()) {
			return expressions;
		}
		String upper = text.toUpperCase(Locale.ROOT);
		StringBuilder current = new StringBuilder();
		String pendingConnector = "";
		int depth = 0;
		boolean inSingle = false;
		boolean inDouble = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'' && !inDouble) {
				current.append(c);
				if (i + 1 < text.length() && text.charAt(i + 1) == '\'') {
					current.append(text.charAt(i + 1));
					i++;
				} else {
					inSingle = !inSingle;
				}
				continue;
			}
			if (c == '"' && !inSingle) {
				current.append(c);
				if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
					current.append(text.charAt(i + 1));
					i++;
				} else {
					inDouble = !inDouble;
				}
				continue;
			}
			if (inSingle || inDouble) {
				current.append(c);
				continue;
			}
			if (c == '(') {
				depth++;
				current.append(c);
				continue;
			}
			if (c == ')') {
				depth = Math.max(depth - 1, 0);
				current.append(c);
				continue;
			}
			if (depth == 0) {
				if (upper.startsWith("AND", i) && isWord(text, i, 3)) {
					addPredicateExpression(expressions, pendingConnector, current.toString());
					pendingConnector = "AND";
					current.setLength(0);
					i += 3;
					i = skipWhitespace(text, i) - 1;
					continue;
				}
				if (upper.startsWith("OR", i) && isWord(text, i, 2)) {
					addPredicateExpression(expressions, pendingConnector, current.toString());
					pendingConnector = "OR";
					current.setLength(0);
					i += 2;
					i = skipWhitespace(text, i) - 1;
					continue;
				}
			}
			current.append(c);
		}
		addPredicateExpression(expressions, pendingConnector, current.toString());
		return expressions;
	}

	private static void addPredicateExpression(List<PredicateExpression> expressions, String connector,
			String expression) {
		String trimmed = expression.trim();
		if (!trimmed.isEmpty()) {
			expressions.add(new PredicateExpression(connector == null ? "" : connector, trimmed));
		}
	}

	private static boolean isWord(String text, int start, int length) {
		int end = start + length;
		boolean leadingBoundary = start <= 0 || !isIdentifierChar(text.charAt(start - 1));
		boolean trailingBoundary = end >= text.length() || !isIdentifierChar(text.charAt(end));
		return leadingBoundary && trailingBoundary;
	}

	private static int matchCompositeKeyword(String upper, String original, int index, String[] words) {
		int pos = index;
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (!upper.startsWith(word, pos)) {
				return -1;
			}
			if (!isWord(original, pos, word.length())) {
				return -1;
			}
			pos += word.length();
			if (i < words.length - 1) {
				pos = skipWhitespace(original, pos);
			}
		}
		return pos - index;
	}

	private static int findKeywordOutsideParens(String text, String keyword) {
		if (text.isBlank()) {
			return -1;
		}
		String upper = text.toUpperCase(Locale.ROOT);
		int depth = 0;
		boolean inSingle = false;
		boolean inDouble = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'' && !inDouble) {
				inSingle = !inSingle;
				continue;
			}
			if (c == '"' && !inSingle) {
				inDouble = !inDouble;
				continue;
			}
			if (inSingle || inDouble) {
				continue;
			}
			if (c == '(') {
				depth++;
				continue;
			}
			if (c == ')') {
				depth = Math.max(depth - 1, 0);
				continue;
			}
			if (depth == 0 && upper.startsWith(keyword, i) && isWord(text, i, keyword.length())) {
				return i;
			}
		}
		return -1;
	}

	private static int findKeywordOutsideParens(String text, String keyword, int startIndex) {
		int relative = findKeywordOutsideParens(text.substring(startIndex), keyword);
		return relative < 0 ? -1 : startIndex + relative;
	}

	private static int skipWhitespace(String text, int index) {
		int pos = index;
		while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
			pos++;
		}
		return pos;
	}

	private static boolean isIdentifierChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}

	private static int consumeBlockComment(String text, int start) {
		int end = start + 2;
		while (end < text.length()) {
			if (text.charAt(end) == '*' && end + 1 < text.length() && text.charAt(end + 1) == '/') {
				return end + 2;
			}
			end++;
		}
		return text.length();
	}

	private static boolean isWrappedInParentheses(String text) {
		if (text.length() < 2 || text.charAt(0) != '(' || text.charAt(text.length() - 1) != ')') {
			return false;
		}
		int depth = 0;
		boolean inSingle = false;
		boolean inDouble = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\'' && !inDouble) {
				inSingle = !inSingle;
				continue;
			}
			if (c == '"' && !inSingle) {
				inDouble = !inDouble;
				continue;
			}
			if (inSingle || inDouble) {
				continue;
			}
			if (c == '(') {
				depth++;
			} else if (c == ')') {
				depth--;
				if (depth == 0 && i < text.length() - 1) {
					return false;
				}
			}
		}
		return depth == 0;
	}

	private record ClauseHeader(String extras, String body) {
	}

	private record PredicateExpression(String connector, String expression) {
	}

	private record WithBlock(List<Cte> ctes, String mainSql) {
	}

	private record Cte(String name, String body) {
	}

    private static String formatLiteral(Object value, Dialect dialect) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Date date) {
            return "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(date) + "'";
        }
        if (value instanceof SqlParameter<?> parameter) {
            throw new IllegalArgumentException("Unbound parameter '" + parameter.name() + "'");
        }
        return "'" + value.toString().replace("'", "''") + "'";
    }

	private static String breakIntoClauses(String sql, PrettyPrintOptions opts) {
		StringBuilder builder = new StringBuilder(sql.length() + 32);
		int depth = 0;
		String upper = sql.toUpperCase(Locale.ROOT);
		List<Boolean> parenBreaks = new ArrayList<>();
		for (int i = 0; i < sql.length();) {
			if (depth == 0 && upper.startsWith("SELECT", i) && isClauseBoundary(upper, i)) {
				builder.append('\n').append(opts.indentString(depth)).append("SELECT");
				i += 6;
				continue;
			}
			if (depth == 0 && upper.startsWith("WITH", i) && isClauseBoundary(upper, i)) {
				builder.append('\n').append(opts.indentString(depth)).append("WITH");
				i += 4;
				continue;
			}
			if (depth == 0 && upper.startsWith("FROM", i) && isClauseBoundary(upper, i)) {
				builder.append('\n').append(opts.indentString(depth)).append("FROM");
				i += 4;
				continue;
			}
			if (depth == 0 && upper.startsWith("WHERE", i) && isClauseBoundary(upper, i)) {
				builder.append('\n').append(opts.indentString(depth)).append("WHERE");
				i += 5;
				continue;
			}
			if (depth == 0 && upper.startsWith("GROUP BY", i) && isClauseBoundary(upper, i)) {
				builder.append('\n').append(opts.indentString(depth)).append("GROUP BY");
				i += 8;
				continue;
			}
			if (depth == 0 && upper.startsWith("HAVING", i) && isClauseBoundary(upper, i)) {
				builder.append('\n').append(opts.indentString(depth)).append("HAVING");
				i += 6;
				continue;
			}
			if (depth == 0 && upper.startsWith("ORDER BY", i) && isClauseBoundary(upper, i)) {
				builder.append('\n').append(opts.indentString(depth)).append("ORDER BY");
				i += 8;
				continue;
			}
			if (depth == 0 && (upper.startsWith("UNION", i) || upper.startsWith("INTERSECT", i) || upper.startsWith("EXCEPT", i))) {
				if (isClauseBoundary(upper, i)) {
					builder.append('\n').append(opts.indentString(depth)).append(sql, i,
							Math.min(sql.length(), i + nextKeywordLength(upper, i)));
					i += nextKeywordLength(upper, i);
					continue;
				}
			}
			char c = sql.charAt(i);
			builder.append(c);
			if (c == '(') {
				String prevKeyword = previousKeyword(upper, i);
				boolean breakAfter = !"JOIN".equals(prevKeyword) && shouldBreakAfterParen(upper, i + 1);
				parenBreaks.add(Boolean.valueOf(breakAfter));
				depth++;
				if (breakAfter) {
					builder.append('\n').append(opts.indentString(depth));
				}
			} else if (c == ')') {
				boolean breakApplied = !parenBreaks.isEmpty() && Boolean.TRUE.equals(parenBreaks.remove(parenBreaks.size() - 1));
				depth = Math.max(depth - 1, 0);
				if (breakApplied && i + 1 < sql.length()) {
					builder.append('\n').append(opts.indentString(depth));
				}
			}
			i++;
		}
		return builder.toString();
	}

	private static boolean shouldBreakAfterParen(String upperSql, int start) {
		int pos = start;
		while (pos < upperSql.length() && Character.isWhitespace(upperSql.charAt(pos))) {
			pos++;
		}
		if (pos >= upperSql.length()) {
			return false;
		}
		return upperSql.startsWith("SELECT", pos) || upperSql.startsWith("WITH", pos) || upperSql.startsWith("VALUES", pos);
	}

	private static String previousKeyword(String upperSql, int index) {
		int i = index - 1;
		while (i >= 0 && Character.isWhitespace(upperSql.charAt(i))) {
			i--;
		}
		int end = i;
		while (i >= 0 && Character.isLetter(upperSql.charAt(i))) {
			i--;
		}
		if (end >= 0 && end > i) {
			return upperSql.substring(i + 1, end + 1);
		}
		return "";
	}

}
