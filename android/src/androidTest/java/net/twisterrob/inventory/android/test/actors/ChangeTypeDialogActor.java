package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;
import org.hamcrest.core.AnyOf;

import static org.hamcrest.Matchers.*;

import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
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

/**
 * @see net.twisterrob.inventory.android.view.ChangeTypeDialog
 */
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
		clickPositiveInDialog();
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
		DataInteraction row = navigateToType(type);
		// Back closes the keyword listing popup if click() turns into longClick().
		row.perform(click(pressBack()));
		if (exists(onView(dialogMatcher))) {
			// We've found what we wanted, selected it, but it's not auto-closing the dialog.
			// This happens when it's not a leaf sub-category, so it needs manual confirmation.
		
			// Self-verify that click() actually selected the type in current iteration.
			assertSelected(typeName);
			// Terminate the dialog, because the actor needs to "select" the type.
			save();
		}
	}

	public @NonNull KeywordsDialogActor showKeywords(int type) {
		navigateToType(type).perform(longClick());
		KeywordsDialogActor dialog = new KeywordsDialogActor();
		dialog.assertDisplayed();
		return dialog;
	}

	private @NonNull DataInteraction navigateToType(int type) {
		String typeName = getApplicationContext().getResources().getResourceEntryName(type);
		// TODO figure out how to get this from the Hilt Singleton component.
		CategoryCache cache = new CategoryCacheHolder(getApplicationContext(), App.db()).getCacheForCurrentLocale();
		while (exists(onView(dialogMatcher))) {
			// If the dialog closes automatically with the click,
			// it means that we've managed to select what we wanted,
			// and it was a leaf sub-category.
			String currentType = typeName;
			do {
				DataInteraction row = onData(withColumn(Category.NAME, currentType));
				if (!exists(row)) {
					// Could not find the type we're looking for.
					// This probably means that it's not expanded yet,
					// continue and try to select a parent first so the sub-category becomes visible.
					continue;
				}
				if (typeName.equals(currentType)) {
					// We've found the type we're looking for, not one of its parents.
					return row;
				}
				// We've found a row we need to click in order to expand the one we need.
				// Click the row, so that we expand it.
				
				// Back closes the keyword listing popup if click() turns into longClick().
				row.perform(click(pressBack()));
				// Self-verify that click() actually selected the type in current iteration.
				assertSelected(currentType);
				break; // Start again, because the dialog contents significantly changed.
			} while ((currentType = cache.getParent(currentType)) != null);
		}
		throw new IllegalStateException("Could not find type " + typeName);
	}
}
