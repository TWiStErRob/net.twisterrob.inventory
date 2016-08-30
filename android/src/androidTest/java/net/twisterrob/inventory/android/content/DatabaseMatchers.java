package net.twisterrob.inventory.android.content;

import org.hamcrest.*;

import android.database.Cursor;
import android.support.annotation.NonNull;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.debug.test.R;

public class DatabaseMatchers {
	public static @NonNull Matcher<? super Database> countImages(final Matcher<? super Long> countMatcher) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB image count is ").appendDescriptionOf(countMatcher);
			}
			@Override protected boolean matchesSafely(Database item, Description mismatch) {
				Cursor cursor = item.rawQuery(R.string.query_image_count);
				Long count = DatabaseTools.singleLong(cursor, null);
				countMatcher.describeMismatch(count, mismatch);
				return countMatcher.matches(count);
			}
		};
	}

	public static @NonNull Matcher<? super Database> hasInvItem(final String itemName) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB has item named ").appendValue(itemName);
			}
			@Override protected boolean matchesSafely(Database item, Description mismatch) {
				Cursor cursor = item.rawQuery(R.string.query_item_exists_by_name, itemName);
				return DatabaseTools.singleBoolean(cursor);
			}
		};
	}
}
