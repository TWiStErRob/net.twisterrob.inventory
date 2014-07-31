package net.twisterrob.inventory.android.activity;

import java.util.*;

import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import net.twisterrob.android.adapter.BaseListAdapter;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;

public class MainActivity extends BaseActivity {
	private GridView list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main_activity);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);

		list = (GridView)findViewById(android.R.id.list);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MainItem item = (MainItem)parent.getAdapter().getItem(position);
				item.listener.onClick(view);
			}
		});
		Collection<MainItem> actions = Arrays.asList( //
				new MainItem("Properties", R.drawable.property_home, new OnClickListener() {
					public void onClick(View v) {
						startActivity(PropertyListActivity.list());
					}
				}), new MainItem("Edit Property #1", R.drawable.property_home, new OnClickListener() {
					public void onClick(View v) {
						startActivity(PropertyEditActivity.edit(1));
					}
				}), new MainItem("Edit Room #4", R.drawable.room_bedroom, new OnClickListener() {
					public void onClick(View v) {
						startActivity(RoomEditActivity.edit(4));
					}
				}), new MainItem("Edit Item #5", R.drawable.room_storage, new OnClickListener() {
					public void onClick(View v) {
						startActivity(ItemEditActivity.edit(5));
					}
				}), new MainItem("View Property #1", R.drawable.property_home, new OnClickListener() {
					public void onClick(View v) {
						startActivity(PropertyViewActivity.list(1));
					}
				}), new MainItem("Drive", android.R.drawable.ic_menu_upload, new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(App.getAppContext(), WelcomeActivity.class);
						startActivity(intent);
					}
				}), new MainItem("Camera", android.R.drawable.ic_menu_camera, new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(App.getAppContext(), CaptureImage.class);
						startActivity(intent);
					}
				}));
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
					((MainItem)list.getItemAtPosition(4)).listener.onClick(list);
				}
			}, 1000);
		}
	}
	private static class MainItem {
		public final CharSequence title;
		public final int iconDrawable;
		public final OnClickListener listener;

		public MainItem(String title, int iconDrawable, OnClickListener listener) {
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
			holder.icon.setImageResource(currentItem.iconDrawable);
		}
	}
}
