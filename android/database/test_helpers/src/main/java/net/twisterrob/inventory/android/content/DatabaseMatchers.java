package net.twisterrob.inventory.android.content;

import java.util.*;

import org.hamcrest.*;

import android.database.Cursor;
import android.support.annotation.*;

import net.twisterrob.android.utils.tools.DatabaseTools;
import net.twisterrob.inventory.android.database.test_helpers.R;

@VisibleForTesting
// TODO refactor to merge with DataBaseActor
public class DatabaseMatchers {
	public static @NonNull Matcher<? super Database> countImages(final Matcher<? super Long> countMatcher) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB image count is ").appendDescriptionOf(countMatcher);
			}
			@Override protected boolean matchesSafely(Database db, Description mismatch) {
				Cursor cursor = db.rawQuery(R.string.query_image_count);
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
			@Override protected boolean matchesSafely(Database db, Description mismatch) {
				Cursor cursor = db.rawQuery(R.string.query_item_exists_by_name, itemName);
				return DatabaseTools.singleBoolean(cursor);
			}
		};
	}
	public static @NonNull Matcher<? super Database> hasInvItemIn(final String parentName, final String childName) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB has item named ").appendValue(childName)
				           .appendText(" in item ").appendValue(parentName);
			}
			@Override protected boolean matchesSafely(Database db, Description mismatch) {
				Cursor cursor = db.rawQuery(R.string.query_item_exists_by_parent, parentName, childName);
				if (!DatabaseTools.singleBoolean(cursor)) {
					mismatch.appendText("found item named ").appendValue(childName);
					appendParents(mismatch, db, childName, R.string.query_item_parent);
					return false;
				}
				return true;
			}
		};
	}
	public static @NonNull Matcher<? super Database> hasInvItemInRoom(final String roomName, final String itemName) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB has item named ").appendValue(itemName)
				           .appendText(" in room ").appendValue(roomName);
			}
			@Override protected boolean matchesSafely(Database db, Description mismatch) {
				Cursor cursor = db.rawQuery(R.string.query_item_exists_by_room, roomName, itemName);
				if (!DatabaseTools.singleBoolean(cursor)) {
					mismatch.appendText("found item named ").appendValue(itemName);
					appendParents(mismatch, db, itemName, R.string.query_item_parent);
					return false;
				}
				return true;
			}
		};
	}

	public static @NonNull Matcher<? super Database> hasInvRoom(final String roomName) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB has room named ").appendValue(roomName);
			}
			@Override protected boolean matchesSafely(Database db, Description mismatch) {
				Cursor cursor = db.rawQuery(R.string.query_room_exists_by_name, roomName);
				return DatabaseTools.singleBoolean(cursor);
			}
		};
	}
	public static @NonNull Matcher<? super Database> hasInvRoomInProperty(final String propertyName,
			final String roomName) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB has room named ").appendValue(roomName)
				           .appendText(" in property ").appendValue(propertyName);
			}
			@Override protected boolean matchesSafely(Database db, Description mismatch) {
				Cursor cursor = db.rawQuery(R.string.query_room_exists_by_property, propertyName, roomName);
				if (!DatabaseTools.singleBoolean(cursor)) {
					mismatch.appendText("found room named ").appendValue(roomName);
					appendParents(mismatch, db, roomName, R.string.query_room_parent);
					return false;
				}
				return true;
			}
		};
	}

	public static @NonNull Matcher<? super Database> hasInvProperty(final String propertyName) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB has property named ").appendValue(propertyName);
			}
			@Override protected boolean matchesSafely(Database db, Description mismatch) {
				Cursor cursor = db.rawQuery(R.string.query_property_exists_by_name, propertyName);
				return DatabaseTools.singleBoolean(cursor);
			}
		};
	}

	public static @NonNull Matcher<? super Database> hasInvList(final String listName) {
		return new TypeSafeDiagnosingMatcher<Database>() {
			@Override public void describeTo(Description description) {
				description.appendText("DB has list named ").appendValue(listName);
			}
			@Override protected boolean matchesSafely(Database db, Description mismatch) {
				Cursor cursor = db.rawQuery(R.string.query_list_exists_by_name, listName);
				return DatabaseTools.singleBoolean(cursor);
			}
		};
	}

	@SuppressWarnings("TryFinallyCanBeTryWithResources")
	private static void appendParents(Description mismatch,
			Database db, String belongingName, @StringRes int parentQuery) {
		Cursor debug = db.rawQuery(parentQuery, belongingName);
		try {
			List<String> possibilities = new ArrayList<>();
			while (debug.moveToNext()) {
				possibilities.add(DatabaseTools.getString(debug, "parent"));
			}
			mismatch.appendText(" was in ").appendValueList("", " and ", "", possibilities);
		} finally {
			debug.close();
		}
	}
}
