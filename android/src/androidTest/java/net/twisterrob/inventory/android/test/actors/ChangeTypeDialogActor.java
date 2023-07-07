package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;
import org.hamcrest.core.AnyOf;

import static org.hamcrest.Matchers.*;

import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.StringRes;
import androidx.test.espresso.*;

import static androidx.test.core.app.ApplicationProvider.*;
import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.categories.cache.CategoryCache;
import net.twisterrob.inventory.android.categories.cache.CategoryCacheHolder;
import net.twisterrob.inventory.android.content.contract.Category;

import static net.twisterrob.android.test.espresso.DialogMatchers.*;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class ChangeTypeDialogActor {
	public static final AnyOf<View> titleAssertion = anyOf(
			withText(R.string.item_categorize_title_many),
			withText(formattedRes(R.string.item_categorize_title)),
			withText(formattedRes(R.string.room_change_type)),
			withText(formattedRes(R.string.property_change_type))
	);
	private static final Matcher<View> dialogMatcher =
			allOf(isDialogView(), hasDescendant(allOf(isDialogTitle(), titleAssertion)));

	public void assertOpen() {
		onView(isDialogTitle()).check(matches(titleAssertion));
	}
	public void assertClosed() {
		onView(dialogMatcher).check(doesNotExist());
	}
	public void cancel() {
		Espresso.pressBack();
	}
	public void save() {
		onView(withId(R.id.btn_save)).perform(click());
	}

	public void assertNoneSelected() {
		onView(isAssignableFrom(AdapterView.class)).check(matches(checkedPosition(invalidPosition())));
	}
	public void assertSelected(@StringRes int type) {
		String typeName = getApplicationContext().getResources().getResourceEntryName(type);
		assertSelected(typeName);
	}
	private void assertSelected(String typeName) {
		onData(withColumn(Category.NAME, typeName)).check(matches(isItemChecked()));
	}

	public void select(@StringRes int type) {
		String typeName = getApplicationContext().getResources().getResourceEntryName(type);
		// TODO figure out how to get this from the Hilt Singleton component.
		CategoryCache cache = new CategoryCacheHolder(getApplicationContext(), App.db()).getCacheForCurrentLocale();
		while (exists(onView(dialogMatcher))) {
			// if the first one goes through with the click, it means that we've managed to selected what we wanted
			String currentType = typeName;
			do {
				DataInteraction row = onData(withColumn(Category.NAME, currentType));
				if (!exists(row)) {
					// Not expanded yet, continue and try to select parent first.
					continue;
				}
				// Make sure to close the keyword listing popup if click() turns into longClick().
				row.perform(click(pressBack()));
				if (exists(onView(dialogMatcher))) {
					// Self-verify that click() actually selected the type in current iteration.
					assertSelected(currentType);
				}
				break;
			} while ((currentType = cache.getParent(currentType)) != null);
			if (typeName.equals(currentType) && exists(onView(dialogMatcher))) {
				// selected type needs manual confirmation
				save();
			}
		}
	}
}
