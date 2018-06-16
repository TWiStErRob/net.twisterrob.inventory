package net.twisterrob.inventory.android.activity;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.test.InventoryActivityRule;
import net.twisterrob.inventory.android.test.actors.*;
import net.twisterrob.inventory.android.test.categories.*;

import static net.twisterrob.inventory.android.content.Constants.*;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest_Search {
	@Rule public final ActivityTestRule<MainActivity> activity
			= new InventoryActivityRule<MainActivity>(MainActivity.class) {
		@Override protected void setDefaults() {
			super.setDefaults();
			long itemID = db.create(TEST_PROPERTY, TEST_ROOM, TEST_ITEM);
			getStartIntent().putExtras(Intents.bundleFromParent(itemID));
		}
	};
	@Rule public final DataBaseActor db = new DataBaseActor();
	private final MainActivityActor main = new MainActivityActor();

	@Category({Op.Cancels.class})
	@Test public void testClose() {
		SearchActor search = main.openSearch();
		search.close();
	}

	@Category({Op.Cancels.class})
	@Test public void testTypeClose() {
		SearchActor search = main.openSearch();
		search.setQuery("search");
		search.close();
	}

	@Category({Op.Cancels.class})
	@Test public void testTypeClearClose() {
		SearchActor search = main.openSearch();
		search.setQuery("search");
		search.clear();
		search.close();
	}

	@Ignore("search query is not restored on rotation")
	@Category({Op.Rotates.class})
	@Test public void testRotate() {
		SearchActor search = main.openSearch();
		search.setQuery("search");
		main.rotate();
		search.assertQuery(is("search"));
		main.rotate();
		search.assertQuery(is("search"));
	}

	@Ignore("search query is not restored")
	@Category({Op.Cancels.class})
	@Test public void testSelectClose() {
		SearchActor search = main.openSearch();
		search.setQuery(TEST_ITEM);
		search.select(containsString(TEST_ITEM)).close();
		search.assertQuery(is(TEST_ITEM));
	}

	@Category({Op.Cancels.class})
	@Test public void testSearchClose() {
		SearchActor search = main.openSearch();
		search.setQuery(TEST_ITEM);
		SearchResultActivityActor results = search.search();
		results.openResult(TEST_ITEM).close();
		results.close();
		search.assertClosed();
	}

	@Category({On.Item.class})
	@Test public void testFinds() {
		SearchActor search = main.openSearch();
		search.setQuery(TEST_ITEM);
		search.assertResultShown(containsString(TEST_ITEM));
	}

	@Category({On.Item.class})
	@Test public void testSearchFinds() {
		SearchActor search = main.openSearch();
		search.setQuery(TEST_ITEM);
		SearchResultActivityActor results = search.search();
		results.assertResultShown(TEST_ITEM);
	}

	@Category({On.Item.class, UseCase.Prefs.class})
	@Test public void testOpensAuto() {
		App.prefs().setString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_auto);
		searchAndOpen().assertDetailsVisible();
	}

	@Category({On.Item.class, UseCase.Prefs.class})
	@Test public void testOpensImage() {
		App.prefs().setString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_image);
		searchAndOpen().assertImageVisible();
	}

	@Category({On.Item.class, UseCase.Prefs.class})
	@Test public void testOpensDetails() {
		App.prefs().setString(R.string.pref_defaultViewPage, R.string.pref_defaultViewPage_details);
		searchAndOpen().assertDetailsVisible();
	}

	private ItemViewActivityActor searchAndOpen() {
		SearchActor search = main.openSearch();
		search.setQuery(TEST_ITEM);
		SearchResultActivityActor results = search.search();
		ItemViewActivityActor item = results.openResult(TEST_ITEM);
		item.assertShowing(TEST_ITEM);
		return item;
	}
}
