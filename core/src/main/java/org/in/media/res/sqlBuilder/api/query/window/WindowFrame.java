package org.in.media.res.sqlBuilder.api.query.window;

import java.util.Objects;

public final class WindowFrame {

	public enum Unit {
		ROWS, RANGE
	}

	public enum BoundType {
		UNBOUNDED_PRECEDING,
		PRECEDING,
		CURRENT_ROW,
		FOLLOWING,
		UNBOUNDED_FOLLOWING
	}

	public static final class Bound {

		private final BoundType type;
		private final Integer offset;

		private Bound(BoundType type, Integer offset) {
			this.type = Objects.requireNonNull(type, "type");
			this.offset = offset;
			if ((type == BoundType.PRECEDING || type == BoundType.FOLLOWING) && (offset == null || offset < 0)) {
				throw new IllegalArgumentException("PRECEDING/FOLLOWING bounds require a non-negative offset");
			}
			if ((type == BoundType.UNBOUNDED_PRECEDING || type == BoundType.UNBOUNDED_FOLLOWING
					|| type == BoundType.CURRENT_ROW) && offset != null) {
				throw new IllegalArgumentException("Offset is not expected for " + type);
			}
		}

		public static Bound unboundedPreceding() {
			return new Bound(BoundType.UNBOUNDED_PRECEDING, null);
		}

		public static Bound unboundedFollowing() {
			return new Bound(BoundType.UNBOUNDED_FOLLOWING, null);
		}

		public static Bound currentRow() {
			return new Bound(BoundType.CURRENT_ROW, null);
		}

		public static Bound preceding(int offset) {
			return new Bound(BoundType.PRECEDING, offset);
		}

		public static Bound following(int offset) {
			return new Bound(BoundType.FOLLOWING, offset);
		}

		public BoundType type() {
			return type;
		}

		public Integer offset() {
			return offset;
		}
	}

	private final Unit unit;
	private final Bound start;
	private final Bound end;

	private WindowFrame(Unit unit, Bound start, Bound end) {
		this.unit = Objects.requireNonNull(unit, "unit");
		this.start = Objects.requireNonNull(start, "start");
		this.end = Objects.requireNonNull(end, "end");
	}

	public static WindowFrame rowsBetween(Bound start, Bound end) {
		return new WindowFrame(Unit.ROWS, start, end);
	}

	public static WindowFrame rangeBetween(Bound start, Bound end) {
		return new WindowFrame(Unit.RANGE, start, end);
	}

	public Unit unit() {
		return unit;
	}

	public Bound start() {
		return start;
	}

	public Bound end() {
		return end;
	}
}
