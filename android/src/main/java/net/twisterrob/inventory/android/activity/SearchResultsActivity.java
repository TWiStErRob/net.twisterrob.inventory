package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;

import androidx.appcompat.widget.SearchView;
import androidx.core.app.TaskStackBuilder;
import dagger.hilt.android.AndroidEntryPoint;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.android.wiring.CollapseActionViewOnSubmit;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.ItemViewActivity;
import net.twisterrob.inventory.android.fragment.data.*;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

@AndroidEntryPoint
public class SearchResultsActivity extends SingleFragmentActivity<ItemListFragment> implements ItemsEvents {
	private static final Logger LOG = LoggerFactory.getLogger(SearchResultsActivity.class);
	private SearchView search;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarTitle(getTitle());
	}
	@Override protected ItemListFragment onCreateFragment() {
		return handleIntent();
	}

	@Override protected void onNewIntent(Intent intent) {
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
			setActionBarSubtitle(query);
			return ItemListFragment.newSearchInstance(query);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Uri uri = fixUri(intent.getData());
			LOG.debug("Search '{}' redirecting VIEW for {}", query, uri);
			Intent redirect = new Intent(Intent.ACTION_VIEW, uri);
			redirect.putExtra(BaseViewFragment.KEY_PAGE, getString(R.string.pref_defaultViewPage_details));
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

	@Override public boolean onCreateOptionsMenu(Menu menu) {
		if (!super.onCreateOptionsMenu(menu)) {
			return false;
		}
		getMenuInflater().inflate(R.menu.search, menu);
		search = (SearchView)AndroidTools.prepareSearch(this, menu, R.id.search);
		search.setQueryRefinementEnabled(true);
		search.setOnSearchClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				search.setQuery(getExtraQuery(getIntent()), false);
			}
		});
		search.setOnQueryTextListener(new CollapseActionViewOnSubmit(getSupportActionBar()));
		return true;
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
		Intent view = ItemViewActivity.show(itemID);
		view.putExtra(BaseViewFragment.KEY_PAGE, getString(R.string.pref_defaultViewPage_details));
		startActivity(view);
	}

	public void itemActioned(long itemID) {
		itemSelected(itemID);
	}

	public void newItem(long parentID) {
		throw new UnsupportedOperationException("Cannot create new item here");
	}

	public static Intent show() {
		return new Intent(App.getAppContext(), SearchResultsActivity.class);
	}
}
