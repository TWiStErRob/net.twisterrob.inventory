package net.twisterrob.inventory.android.activity;

import java.io.*;
import java.util.*;

import android.app.SearchManager;
import android.content.*;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import net.twisterrob.android.adapter.BaseListAdapter;
import net.twisterrob.inventory.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.*;
import net.twisterrob.inventory.android.activity.dev.DeveloperActivity;
import net.twisterrob.inventory.android.content.contract.Category;
import net.twisterrob.inventory.android.content.io.csv.*;
import net.twisterrob.inventory.android.fragment.*;
import net.twisterrob.inventory.android.fragment.BackupPickerFragment.BackupPickerListener;

public class MainActivity extends BaseActivity implements BackupPickerListener {
	private GridView list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);

		list = (GridView)findViewById(android.R.id.list);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MainItem item = (MainItem)parent.getItemAtPosition(position);
				item.listener.onClick(view);
			}
		});
		Collection<MainItem> actions = new ArrayList<MainItem>(Arrays.asList( //
				new MainItem(getString(R.string.property_list), App.pic().getSVG(R.raw.property_home),
						new OnClickListener() {
							public void onClick(View v) {
								startActivity(PropertyListActivity.list());
							}
						}), //
				new MainItem(getString(R.string.category_list), App.pic().getSVG(R.raw.category_unknown),
						new OnClickListener() {
							public void onClick(View v) {
								startActivity(CategoryViewActivity.show(Category.INTERNAL));
							}
						}), //
				new MainItem(getString(R.string.item_list), App.pic().getSVG(R.raw.category_collectibles),
						new OnClickListener() {
							public void onClick(View v) {
								startActivity(CategoryItemsActivity.show(Category.INTERNAL));
							}
						}), //
				new MainItem(getString(R.string.sunburst_title), App.pic().getSVG(R.raw.category_disc),
						new OnClickListener() {
							public void onClick(View v) {
								startActivity(SunBurstActivity.show());
							}
						}) //
				));

		if (BuildConfig.DEBUG) {
			actions.add(new MainItem("DEVELOPER", android.R.drawable.ic_menu_more, new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(App.getAppContext(), DeveloperActivity.class);
					startActivity(intent);
				}
			}));
		}
		list.setAdapter(new MainItemAdapter(this, actions));
	}

	private static boolean first = true;
	@Override
	protected void onResume() {
		super.onResume();
		if (first) {
			first = false;
			new Handler().postDelayed(new Runnable() {
				public void run() {
					//((MainItem)list.getItemAtPosition(4)).listener.onClick(list);
				}
			}, 1000);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);

		SearchManager searchManager = (SearchManager)getSystemService(SEARCH_SERVICE);
		SearchView searchView = (SearchView)MenuItemCompat.getActionView(menu.findItem(R.id.search));
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.preferences:
				startActivity(PreferencesActivity.show());
				break;
			case R.id.exportDrive:
				startActivity(ExportActivity.chooser());
				break;
			case R.id.exportSDCard:
				export();
				break;
			case R.id.exportSDCardRemove: {
				pickMode = PickMode.Remove;
				BackupPickerFragment dialog = BackupPickerFragment.choose("Select a backup to remove", ".csv");
				dialog.show(getSupportFragmentManager(), BackupPickerFragment.class.getSimpleName());
				break;
			}
			case R.id.importDB: {
				pickMode = PickMode.Import;
				BackupPickerFragment dialog = BackupPickerFragment.choose("Select a backup to restore", ".csv");
				dialog.show(getSupportFragmentManager(), BackupPickerFragment.class.getSimpleName());
				break;
			}
			default:
				// let super do its thing
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	private void export() {
		String fileName = String.format(Locale.ROOT, Constants.EXPORT_FILE_NAME_FORMAT, Calendar.getInstance());

		File path = new File(App.getInstance().getPhoneHome(), Constants.EXPORT_SDCARD_FOLDER);
		path.mkdirs();

		File file = new File(path, fileName);
		try {
			new DatabaseCSVExporter().export(new FileOutputStream(file));
			App.toast("Exported successfully to " + file);
		} catch (IOException ex) {
			ex.printStackTrace();
			App.toast("Export failed: " + ex.getMessage());
		}
	}

	enum PickMode {
		Import,
		Remove;
	}
	PickMode pickMode;
	public void filePicked(File file) {
		if (pickMode == null) {
			throw new IllegalStateException("Access this method through a dialog's callback");
		}
		switch (pickMode) {
			case Import: {
				DatabaseCSVImporter importer = null;
				try {
					@SuppressWarnings("resource")
					InputStream input = new FileInputStream(file);
					importer = new DatabaseCSVImporter();
					importer.importAll(input);
					App.toast("Import successful from " + file.getName());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				break;
			}
			case Remove: {
				file.delete();
				App.toast("Backup removed successfully: " + file.getName());
				break;
			}
			default:
				throw new UnsupportedOperationException(pickMode + " is not implemented");
		}
		pickMode = null;
	}

	private class MainItem {
		public final CharSequence title;
		public final Drawable iconDrawable;
		public final OnClickListener listener;

		public MainItem(String title, int iconDrawable, OnClickListener listener) {
			this(title, getResources().getDrawable(iconDrawable), listener);

		}
		public MainItem(String title, Drawable iconDrawable, OnClickListener listener) {
			this.title = title;
			this.iconDrawable = iconDrawable;
			this.listener = listener;
		}
	}
	private static class MainItemAdapter extends BaseListAdapter<MainItem, MainItemAdapter.ViewHolder> {
		public MainItemAdapter(Context context, Collection<MainItem> items) {
			super(context, items);
		}

		class ViewHolder {
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
			holder.label.setText(currentItem.title);
			holder.icon.setImageDrawable(currentItem.iconDrawable);
		}
	}
}
