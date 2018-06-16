package net.twisterrob.android.test.matchers;

import org.hamcrest.*;

import static org.hamcrest.Condition.*;

import android.database.Cursor;

import net.twisterrob.android.annotation.CursorFieldType;
import net.twisterrob.android.utils.tools.*;

public class CursorHasColumn<T> extends TypeSafeDiagnosingMatcher<Cursor> {
	private final String columnName;
	private final Class<T> valueClass;
	private final Matcher<T> valueMatcher;

	public CursorHasColumn(String columnName, Class<T> valueClass, Matcher<T> valueMatcher) {
		this.columnName = columnName;
		this.valueClass = valueClass;
		this.valueMatcher = valueMatcher;
	}

	@Override protected boolean matchesSafely(Cursor cursor, Description mismatchDescription) {
		return matched(cursor, mismatchDescription)
				.then(ensurePositionedOnData())
				.and(positionedOnData())
				.and(withColumn(columnName))
				.then(withColumnType(columnName, valueClass))
				.matching(valueMatcher, "value of column '" + columnName + "' ");
	}
	@Override public void describeTo(Description description) {
		description
				.appendText("with ").appendText(valueClass.getSimpleName())
				.appendText(" column named ").appendValue(columnName)
				.appendText(" that ").appendDescriptionOf(valueMatcher);
	}

	private static Condition.Step<Cursor, Cursor> ensurePositionedOnData() {
		return new Condition.Step<Cursor, Cursor>() {
			@Override public Condition<Cursor> apply(Cursor cursor, Description mismatch) {
				if (cursor.isBeforeFirst()) {
					if (!cursor.moveToFirst()) {
						mismatch.appendText("Cannot move to first row.");
						return notMatched();
					}
				}
				return matched(cursor, mismatch);
			}
		};
	}

	private static Condition.Step<Cursor, Cursor> positionedOnData() {
		return new Condition.Step<Cursor, Cursor>() {
			@Override public Condition<Cursor> apply(Cursor cursor, Description mismatch) {
				if (cursor.isBeforeFirst()) {
					mismatch.appendText("positioned before first");
					return notMatched();
				} else if (cursor.isAfterLast()) {
					mismatch.appendText("positioned after last");
					return notMatched();
				} else if (cursor.getPosition() < 0) {
					mismatch.appendText("position is negative");
					return notMatched();
				} else if (cursor.getCount() <= cursor.getPosition()) {
					mismatch.appendText("position is greater than count");
					return notMatched();
				}
				return matched(cursor, mismatch);
			}
		};
	}

	private static Condition.Step<Cursor, Cursor> withColumn(final String columnName) {
		return new Condition.Step<Cursor, Cursor>() {
			@Override public Condition<Cursor> apply(Cursor cursor, Description mismatch) {
				int columnIndex = cursor.getColumnIndex(columnName);
				if (columnIndex == DatabaseTools.INVALID_COLUMN) {
					mismatch
							.appendText("No column named ")
							.appendValue(columnName)
							.appendText(" can be found in ")
							.appendValueList("[", ", ", "]", cursor.getColumnNames());
					return notMatched();
				}
				return matched(cursor, mismatch);
			}
		};
	}

	private static <T> Condition.Step<Cursor, T> withColumnType(
			final String columnName, final Class<T> expectedColumnType) {
		return new Condition.Step<Cursor, T>() {
			@Override public Condition<T> apply(Cursor cursor, Description mismatch) {
				CursorColumnType type = CursorColumnType.fromClass(expectedColumnType);
				int columnType = DatabaseTools.getType(cursor, columnName);
				if (type.getFieldType() != columnType) {
					mismatch
							.appendText("has column ")
							.appendValue(columnName)
							.appendText(" with invalid type ")
							.appendValue(CursorFieldType.Converter.toString(columnType))
							.appendText(", expected type is ")
							.appendValue(expectedColumnType);
					return notMatched();
				}
				@SuppressWarnings("unchecked") T value = (T)type.getValue(cursor, columnName);
				return matched(value, mismatch);
			}
		};
	}
}
