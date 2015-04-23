package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class SearchResultsActivity extends BaseDetailActivity<ItemListFragment> implements ItemsEvents {
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsActivity.class);

	@Override
	protected ItemListFragment onCreateFragment(Bundle savedInstanceState) {
		return handleIntent();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		LOG.trace("onNewIntent(\n{} {}\n->\n{} {}\n)",
				getIntent(), getIntent().getExtras(), intent, intent.getExtras());
		setIntent(intent);
		super.onNewIntent(intent);
		ItemListFragment fragment = handleIntent();
		if (fragment != null) {
			updateFragment(fragment).commit();
		}
	}

	private ItemListFragment handleIntent() {
		Intent intent = getIntent();
		CharSequence query = getExtraQuery(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			LOG.debug("Search '{}'", query);
			setActionBarTitle(getTitle());
			setActionBarSubtitle(query);
			return ItemListFragment.newSearchInstance(query);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = fixUri(intent.getData());
			LOG.debug("Search '{}' redirecting VIEW for {}", query, uri);
			Intent redirect = new Intent(Intent.ACTION_VIEW, uri);
			if (Constants.DISABLE) {
				TaskStackBuilder.create(getApplicationContext())
				                .addNextIntentWithParentStack(redirect)
				                .startActivities();
				finish();
			} else {
				startActivity(redirect);
				finish();
			}
		}
		return null;
	}

	private CharSequence getExtraQuery(Intent intent) {
		CharSequence userTypedQuery = intent.getCharSequenceExtra(SearchManager.USER_QUERY);
		CharSequence searchQuery = intent.getCharSequenceExtra(SearchManager.QUERY);
		return searchQuery != null? searchQuery : userTypedQuery;
	}

	private Uri fixUri(Uri uri) {
		String resolvedAuthority = uri.getAuthority().replace("${applicationId}", BuildConfig.APPLICATION_ID);
		return uri.buildUpon()
		          .authority(resolvedAuthority)
		          .build()
				;
	}

	public void itemSelected(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}

	public void itemActioned(long itemID) {
		itemSelected(itemID);
	}

	public void newItem(long parentID) {
		throw new UnsupportedOperationException("Cannot create new item here");
	}

	public static Intent show() {
		Intent intent = new Intent(App.getAppContext(), SearchResultsActivity.class);
		return intent;
	}
}
