package net.twisterrob.inventory.android.activity;

import java.io.File;
import java.util.*;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import net.twisterrob.android.adapter.BaseListAdapter;
import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.activity.dev.DeveloperActivity;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;

import static net.twisterrob.inventory.android.content.contract.Category.*;

public class MainActivity extends BaseActivity implements BackupPickerListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportFragmentManager() //
				.beginTransaction() //
				.add(new BackupFragment(), BackupFragment.class.getName()) //
				.commit() //
		;

		GridView list = (GridView)findViewById(android.R.id.list);
		list.setAdapter(new MainItemAdapter(this, createActions()));
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MainItem item = (MainItem)parent.getItemAtPosition(position);
				startActivity(item.intent);
			}
		});
	}

	private static Collection<MainItem> createActions() {
		Collection<MainItem> actions = new ArrayList<MainItem>();
		actions.add(new MainItem(R.string.property_list, R.raw.property_home, PropertyListActivity.list()));
		actions.add(new MainItem(R.string.category_list, R.raw.category_unknown, CategoryViewActivity.show(INTERNAL)));
		actions.add(new MainItem(R.string.item_list, R.raw.category_collectibles, CategoryItemsActivity.show(INTERNAL)));
		actions.add(new MainItem(R.string.sunburst_title, R.raw.category_disc, SunBurstActivity.show()));

		if (BuildConfig.DEBUG) {
			actions.add(new MainItem(R.string.dev_title, R.raw.category_electric, DeveloperActivity.show()));
		}
		return actions;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);

		getMenuInflater().inflate(R.menu.search, menu);
		AndroidTools.prepareSearch(this, menu, R.id.search);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.preferences:
				startActivity(PreferencesActivity.show());
				return true;
			default:
				// let super do its thing
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void filePicked(File file) {
		BackupFragment backup = getFragment(BackupFragment.class.getName());
		backup.filePicked(file);
	}

	private static class MainItem {
		private final int titleResourceID;
		private final int svgResourceID;
		private final Intent intent;

		public MainItem(int titleResourceID, int svgResourceID, Intent intent) {
			this.titleResourceID = titleResourceID;
			this.svgResourceID = svgResourceID;
			this.intent = intent;
		}
	}

	private static class MainItemAdapter extends BaseListAdapter<MainItem, MainItemAdapter.ViewHolder> {
		public MainItemAdapter(Context context, Collection<MainItem> items) {
			super(context, items);
		}

		private static class ViewHolder {
			ImageView icon;
			TextView label;
		}

		@Override
		protected int getItemLayoutId() {
			return R.layout.main_item;
		}

		@Override
		protected ViewHolder createHolder(View convertView) {
			ViewHolder holder = new ViewHolder();
			holder.icon = (ImageView)convertView.findViewById(R.id.icon);
			holder.label = (TextView)convertView.findViewById(R.id.title);
			return holder;
		}

		@Override
		protected void bindView(ViewHolder holder, MainItem currentItem, View convertView) {
			holder.label.setText(currentItem.titleResourceID);
			App.pic().loadSVG(m_context, currentItem.svgResourceID).into(holder.icon);
		}
	}
}
