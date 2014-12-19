package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class SearchResultsActivity extends BaseDetailActivity<ItemListFragment>
		implements ItemsEvents, OnBackStackChangedListener {
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsActivity.class);

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().addOnBackStackChangedListener(this);
	}
	@Override
	protected ItemListFragment onCreateFragment(Bundle savedInstanceState) {
		setActionBarTitle(getTitle());
		LOG.trace("onCreate({})", getIntent());
		return handleIntent();
	}

	@Override public void onBackStackChanged() {
		ItemListFragment fragment = getFragment();
		if (fragment != null) {
			String query = fragment.getArguments().getString(SearchManager.QUERY);
			setActionBarSubtitle(query);
		}
	}

	@Override protected void onResume() {
		super.onResume();
		onBackStackChanged();
	}
	@Override
	protected void onNewIntent(Intent intent) {
		LOG.trace("onNewIntent({}->{})", getIntent(), intent);
		setIntent(intent);
		super.onNewIntent(intent);
		ItemListFragment fragment = handleIntent();
		if (fragment != null) {
			updateFragment(fragment)
					.addToBackStack(null)
					.commit()
			;
		}
	}

	private ItemListFragment handleIntent() {
		Intent intent = getIntent();
		CharSequence query = getExtraQuery(intent);
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			LOG.debug("Search '{}'", query);
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
