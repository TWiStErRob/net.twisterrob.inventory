package net.twisterrob.android.test.matchers;

import org.hamcrest.*;

import android.database.Cursor;

import net.twisterrob.android.db.DatabaseOpenHelper;

public class CursorHasColumn extends TypeSafeDiagnosingMatcher<Cursor> {
	private final String columnName;
	private final Matcher<Object> valueMatcher;

	public CursorHasColumn(String columnName, Matcher<?> valueMatcher) {
		this.columnName = columnName;
		this.valueMatcher = nastyGenericsWorkaround(valueMatcher);
	}

	@Override public boolean matchesSafely(Cursor cursor, Description mismatch) {
		if (cursor.isBeforeFirst()) {
			if (!cursor.moveToFirst()) {
				mismatch.appendText("Cannot move to first row.");
				return false;
			}
		}
		int index = cursor.getColumnIndex(columnName);
		if (index == DatabaseOpenHelper.CURSOR_NO_COLUMN) {
			mismatch
					.appendText("No column named ")
					.appendValue(columnName)
					.appendText(" can be found in ")
					.appendValueList("[", ", ", "]", cursor.getColumnNames());
			return false;
		}

		String value = cursor.getString(index);
		if (!valueMatcher.matches(value)) {
			valueMatcher.describeMismatch(value, mismatch);
			return false;
		}

		return true;
	}

	@Override public void describeTo(Description description) {
		description.appendText("hasColumn(").appendValue(columnName).appendText(", ")
		           .appendDescriptionOf(valueMatcher).appendText(")");
	}

	@SuppressWarnings("unchecked")
	private static Matcher<Object> nastyGenericsWorkaround(Matcher<?> valueMatcher) {
		return (Matcher<Object>)valueMatcher;
	}

	public static Matcher<Cursor> hasColumn(String columnName, Matcher<?> valueMatcher) {
		return new CursorHasColumn(columnName, valueMatcher);
	}
}
