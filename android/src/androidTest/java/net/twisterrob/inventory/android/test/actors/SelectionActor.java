package net.twisterrob.inventory.android.test.actors;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

import android.support.v7.widget.ActionBarContextView;
import android.view.View;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.espresso.ReflectiveParentViewMatcher;
import net.twisterrob.inventory.android.R;

import static net.twisterrob.android.test.espresso.EspressoExtensions.doesNotExist;
import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.android.test.matchers.AndroidMatchers.*;

public class SelectionActor {
	private static final Matcher<View> titleMatcher = allOf(
			inContextualActionBar(),
			new ReflectiveParentViewMatcher(isAssignableFrom(ActionBarContextView.class), "mTitleView")
	);
	public void assertIsActive() {
		onView(isContextualActionBar()).check(matches(isCompletelyDisplayed()));
	}
	public void assertInactive() {
		onView(isContextualActionBar()).check(doesNotExist(not(isDisplayed())));
	}
	public void assertSelectionCount(int count) {
		assertIsActive();
		onView(titleMatcher).check(matches(withText(containsWord(String.valueOf(count)))));
	}
	public void hasSelection(String name) {
		onRecyclerItem(withText(name)).check(matches(isSelected()));
	}
	public void hasNoSelection(String name) {
		onRecyclerItem(withText(name)).check(matches(not(isSelected())));
	}
	public void close() {
		onView(withFullResourceName(endsWith(":id/action_mode_close_button"))).perform(click());
		assertInactive();
	}
	public void assertNothingSelected() {
		onView(isRV()).check(itemDoesNotExists(isSelected()));
	}

	public void select(String name) {
		assertIsActive();
		hasNoSelection(name);
		onRecyclerItem(withText(name)).perform(click());
		hasSelection(name);
	}
	public void deselect(String name) {
		assertIsActive();
		hasSelection(name);
		onRecyclerItem(withText(name)).perform(click());
		hasNoSelection(name);
	}
	public void selectAll() {
		assertIsActive();
		onActionMenuView(withText(R.string.selection_selectAll)).perform(click());
	}
	public void invertSelection() {
		assertIsActive();
		onActionMenuView(withText(R.string.selection_selectInv)).perform(click());
	}
	public ChangeTypeDialogActor changeType() {
		assertIsActive();
		onActionMenuView(withText(R.string.item_categorize_many)).perform(click());
		ChangeTypeDialogActor actor = new ChangeTypeDialogActor();
		actor.assertOpen();
		return actor;
	}
	public MoveTargetActivityActor move() {
		onView(allOf(withId(R.id.action_item_move), inContextualActionBar())).perform(click());
		MoveTargetActivityActor actor = new MoveTargetActivityActor();
		actor.assertIsInFront();
		return actor;
	}
}
