package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.activity.data.BaseDetailActivity.NoFragment;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class SearchResultsActivity extends BaseDetailActivity<NoFragment, ItemListFragment> implements ItemsEvents {
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsActivity.class);

	@Override
	protected void onCreateFragments(Bundle savedInstanceState) {
		hideDetails();
		LOG.trace("onCreate({})", getIntent());
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		LOG.trace("onNewIntent({}->{})", getIntent(), intent);
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		CharSequence query = getExtraQuery();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			LOG.info("Search '{}'", query);
			setActionBarSubtitle(query);
			updateChildrenFragment(ItemListFragment.newSearchInstance(query)).commit();
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			LOG.info("Search '{}' redirecting VIEW for {}", query, intent.getData());
			startActivity(new Intent(Intent.ACTION_VIEW, intent.getData()));
			finish();
		}
	}

	private CharSequence getExtraQuery() {
		Intent intent = getIntent();
		CharSequence userTypedQuery = intent.getCharSequenceExtra(SearchManager.USER_QUERY);
		CharSequence searchQuery = intent.getCharSequenceExtra(SearchManager.QUERY);
		return searchQuery != null? searchQuery : userTypedQuery;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.search, menu);
		AndroidTools.prepareSearch(this, menu, R.id.search);

		return true;
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
