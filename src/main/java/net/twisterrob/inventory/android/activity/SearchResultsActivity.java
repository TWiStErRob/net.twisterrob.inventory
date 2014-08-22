package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.activity.data.BaseDetailActivity.NoFragment;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class SearchResultsActivity extends BaseDetailActivity<NoFragment, ItemListFragment> implements ItemsEvents {
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsActivity.class);

	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		Intent intent = getIntent();
		String userTypedQuery = intent.getStringExtra(SearchManager.USER_QUERY);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchQuery = intent.getStringExtra(SearchManager.QUERY);
			LOG.info("Search '{}' -> '{}'", userTypedQuery, searchQuery);

			hideDetails();
			setFragments(null, ItemListFragment.newSearchInstance(searchQuery));
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			LOG.info("Search '{}' redirecting VIEW for {}", userTypedQuery, intent.getData());
			startActivity(new Intent(Intent.ACTION_VIEW, intent.getData()));
			finish();
		}
	}

	public void itemSelected(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}

	public void itemActioned(long itemID) {
		startActivity(ItemEditActivity.edit(itemID));
	}

	public void newItem(long parentID) {
		// ignore
	}

	public static Intent show() {
		Intent intent = new Intent(App.getAppContext(), SearchResultsActivity.class);
		return intent;
	}
}
