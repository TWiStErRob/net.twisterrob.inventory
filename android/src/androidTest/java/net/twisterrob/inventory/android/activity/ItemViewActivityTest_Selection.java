package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.FlakyTest;

import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import net.twisterrob.android.test.SkipOnCI;
import net.twisterrob.inventory.android.R;
import net.twisterrob.inventory.android.activity.data.ItemViewActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.android.test.espresso.EspressoExtensions.*;
import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
@Category({On.Item.class, UseCase.Selection.class})
public class ItemViewActivityTest_Selection {

	@SuppressWarnings("deprecation")
	@Rule public final androidx.test.rule.ActivityTestRule<ItemViewActivity> activity
			= new InventoryActivityRule<ItemViewActivity>(ItemViewActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			itemID = db.create(TEST_PROPERTY, TEST_ROOM, TEST_ITEM);
			getStartIntent().putExtras(Intents.bundleFromParent(itemID));
		}
	};
	@Rule public final DataBaseActor db = new AppSingletonDatabaseActor();
	private final ItemViewActivityActor itemView = new ItemViewActivityActor();
	private long itemID;

	@Category({UseCase.InitialCondition.class})
	@Test public void testNotStarted() {
		createItems(3);
		SelectionActor selection = new SelectionActor();
		selection.assertNothingSelected();
		selection.assertInactive();
	}

	@Test public void testStarts() {
		createItems(1);
		SelectionActor selection = itemView.select(subItem(1));
		selection.hasSelection(subItem(1));
		selection.assertSelectionCount(1);
	}

	@Category({Op.Cancels.class})
	@Test public void testExits() {
		createItems(1);
		SelectionActor selection = itemView.select(subItem(1));
		selection.close();
		selection.assertNothingSelected();
	}

	@Category({Op.Cancels.class})
	@Test public void testExitsWhenLastDeselectedSingle() {
		createItems(1);
		SelectionActor selection = itemView.select(subItem(1));
		selection.deselect(subItem(1));
		selection.assertNothingSelected();
		selection.assertInactive();
	}

	@Category({Op.Cancels.class})
	@Test public void testExitsWhenLastDeselected() {
		createItems(2);
		SelectionActor selection = itemView.select(subItem(1));
		selection.deselect(subItem(1));
		selection.assertNothingSelected();
		selection.assertInactive();
	}

	@Category({Op.Cancels.class})
	@Test public void testExitsWhenLastDeselectedMultiple() {
		createItems(9);

		SelectionActor selection = itemView.select(subItem(1));
		selection.select(subItem(3));
		selection.select(subItem(6));
		selection.hasSelection(subItem(1));
		selection.hasNoSelection(subItem(2));
		selection.hasSelection(subItem(3));
		selection.hasNoSelection(subItem(4));
		selection.hasNoSelection(subItem(5));
		selection.hasSelection(subItem(6));
		selection.hasNoSelection(subItem(7));
		selection.hasNoSelection(subItem(8));
		selection.hasNoSelection(subItem(9));

		selection.deselect(subItem(6));
		selection.hasNoSelection(subItem(6));
		selection.deselect(subItem(3));
		selection.hasNoSelection(subItem(3));
		selection.deselect(subItem(1));
		selection.hasNoSelection(subItem(1));

		selection.assertNothingSelected();
		selection.assertInactive();
	}

	@SuppressWarnings("deprecation") // TODO
	@Category({Op.CreatesBelonging.class})
	@Test public void testCreateKeepSelection() {
		db.createItem(itemID, subItem(1));
		db.createItem(itemID, subItem(3));
		db.createItem(itemID, subItem(4));
		db.createItem(itemID, subItem(5));
		itemView.refresh();

		SelectionActor selection = itemView.select(subItem(1));
		selection.select(subItem(3));
		selection.select(subItem(4));
		selection.select(subItem(5));
		selection.hasSelection(subItem(1));
		selection.hasSelection(subItem(3));
		selection.hasSelection(subItem(4));
		selection.hasSelection(subItem(5));

		ItemEditActivityActor creator = itemView.addItem();
		creator.setName(subItem(2));
		creator.save();

		selection.hasSelection(subItem(1));
		selection.hasNoSelection(subItem(2));
		selection.hasSelection(subItem(3));
		selection.hasSelection(subItem(4));
		selection.hasSelection(subItem(5));
	}

	/**
	 * This test is a bit jumpy because hasSelection jumps to the item being verified.
	 */
	@Category({Op.Cancels.class, UseCase.Complex.class})
	@Test public void testExitsRandomSelectionAll() {
		final int count = 9;
		createItems(count);
		SelectionActor selection = itemView.select(subItem(1));
		for (int i = 2; i <= count; i++) {
			selection.select(subItem(i));
			selection.assertSelectionCount(i);
			for (int j = 1; j <= i; j++) {
				selection.hasSelection(subItem(j));
			}
			for (int j = i + 1; j <= count; j++) {
				selection.hasNoSelection(subItem(j));
			}
		}
		selection.assertSelectionCount(count);
		for (int i = 1; i <= count; i++) {
			selection.deselect(subItem(i));
			selection.assertSelectionCount(count - i);
			for (int j = 1; j <= i; j++) {
				selection.hasNoSelection(subItem(j));
			}
			for (int j = i + 1; j <= count; j++) {
				selection.hasSelection(subItem(j));
			}
		}
		selection.assertNothingSelected();
		selection.assertInactive();
	}

	@Test public void testSelectAll() {
		createItems(4);
		SelectionActor selection = itemView.select(subItem(2));
		selection.selectAll();
		selection.hasSelection(subItem(1));
		selection.hasSelection(subItem(2));
		selection.hasSelection(subItem(3));
		selection.hasSelection(subItem(4));
		selection.assertSelectionCount(4);
	}

	@Category({Op.Cancels.class})
	@Test public void testSelectNone() {
		createItems(4);
		SelectionActor selection = itemView.select(subItem(2));
		selection.selectAll();
		selection.invertSelection();
		selection.hasNoSelection(subItem(1));
		selection.hasNoSelection(subItem(2));
		selection.hasNoSelection(subItem(3));
		selection.hasNoSelection(subItem(4));
		selection.assertInactive();
	}

	@Test public void testInvert() {
		createItems(4);

		SelectionActor selection = itemView.select(subItem(1));
		selection.select(subItem(3));
		selection.hasSelection(subItem(1));
		selection.hasNoSelection(subItem(2));
		selection.hasSelection(subItem(3));
		selection.hasNoSelection(subItem(4));

		selection.invertSelection();
		selection.assertIsActive();

		selection.hasSelection(subItem(2));
		selection.hasSelection(subItem(4));
		selection.hasNoSelection(subItem(1));
		selection.hasSelection(subItem(2));
		selection.hasNoSelection(subItem(3));
		selection.hasSelection(subItem(4));
	}

	@Test public void testSelectionDisablesTypeChange() {
		createItems(2);
		SelectionActor selection = itemView.select(subItem(1));
		selection.hasSelection(subItem(1));
		selection.assertSelectionCount(1);

		selection.hasNoSelection(subItem(2));
		onRecyclerItem(withText(subItem(2))).onChildView(withId(R.id.type)).perform(click());
		selection.hasSelection(subItem(2));
		selection.assertSelectionCount(2);

		selection.hasSelection(subItem(1));
		onRecyclerItem(withText(subItem(1))).onChildView(withId(R.id.type)).perform(click());
		selection.hasNoSelection(subItem(1));
		selection.assertSelectionCount(1);
	}

	@SuppressWarnings("deprecation") // TODO
	@Category({UseCase.Complex.class})
	@FlakyTest(bugId = 215, detail = "https://github.com/TWiStErRob/net.twisterrob.inventory/issues/215")
	@SkipOnCI(reason = "Hangs, because it's not able to select an item in the dialog.")
	@Test public void testSelectionTypeChange() {
		createItems(4);
		db.assertItemHasType(subItem(1), TEST_ITEM_CATEGORY_DEFAULT);
		db.setItemCategory(subItem(2), TEST_ITEM_CATEGORY);
		db.setItemCategory(subItem(4), TEST_ITEM_CATEGORY_OTHER);
		itemView.refresh();
		SelectionActor selection = itemView.select(subItem(2));
		selection.select(subItem(4));
		selection.select(subItem(1));
		ChangeTypeDialogActor typeDialog = selection.changeType();
		typeDialog.assertNoneSelected();
		typeDialog.select(TEST_ITEM_CATEGORY_OTHER);
		typeDialog.assertClosed();
		selection.assertInactive();
		db.assertItemHasType(subItem(1), TEST_ITEM_CATEGORY_OTHER);
		db.assertItemHasType(subItem(2), TEST_ITEM_CATEGORY_OTHER);
		db.assertItemHasType(subItem(3), TEST_ITEM_CATEGORY_DEFAULT);
		db.assertItemHasType(subItem(4), TEST_ITEM_CATEGORY_OTHER);
		// TODO verify UI state (icons)
	}

	@SuppressWarnings("deprecation") // TODO
	@FlakyTest(bugId = 215, detail = "https://github.com/TWiStErRob/net.twisterrob.inventory/issues/215")
	@SkipOnCI(reason = "Hangs, because it's not able to select an item in the dialog.")
	@Test public void testSelectionTypeChangeAtOnce() {
		createItems(3);
		db.setItemCategory(subItem(1), TEST_ITEM_CATEGORY);
		db.setItemCategory(subItem(3), TEST_ITEM_CATEGORY);
		itemView.refresh();
		SelectionActor selection = itemView.select(subItem(1));
		selection.select(subItem(3));
		ChangeTypeDialogActor typeDialog = selection.changeType();
		typeDialog.assertSelected(TEST_ITEM_CATEGORY);
		typeDialog.select(TEST_ITEM_CATEGORY_OTHER);
		typeDialog.assertClosed();
		selection.assertInactive();
		db.assertItemHasType(subItem(1), TEST_ITEM_CATEGORY_OTHER);
		db.assertItemHasType(subItem(2), TEST_ITEM_CATEGORY_DEFAULT);
		db.assertItemHasType(subItem(3), TEST_ITEM_CATEGORY_OTHER);
		// TODO verify UI state (icons)
	}

	@SuppressWarnings("deprecation") // TODO
	private void createItems(int count) {
		assertThat(count, greaterThan(0));
		for (int i = 1; i <= count; i++) {
			db.createItem(itemID, subItem(i));
		}
		itemView.refresh();
	}

	private static String subItem(int i) {
		return TEST_SUBITEM + " " + i;
	}
}
