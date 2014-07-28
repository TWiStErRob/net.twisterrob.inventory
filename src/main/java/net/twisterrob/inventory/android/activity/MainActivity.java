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
		super.setContentView(R.layout.main);
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
				new MainItem("Properties", new OnClickListener() {
					public void onClick(View v) {
						startActivity(PropertiesActivity.list());
					}
				}), new MainItem("Edit Item #5", new OnClickListener() {
					public void onClick(View v) {
						startActivity(ItemEditActivity.edit(5));
					}
				}), new MainItem("Drive", new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent(App.getAppContext(), WelcomeActivity.class);
						startActivity(intent);
					}
				}), new MainItem("Camera", new OnClickListener() {
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
					//((MainItem)list.getItemAtPosition(1)).listener.onClick(list);
				}
			}, 1000);
		}
	}
	private static class MainItem {
		public CharSequence title;
		public OnClickListener listener;

		public MainItem(String title, OnClickListener listener) {
			this.title = title;
			this.listener = listener;
		}
	}
	private static class MainItemAdapter extends BaseListAdapter<MainItem, MainItemAdapter.ViewHolder> {
		public MainItemAdapter(Context context, Collection<MainItem> items) {
			super(context, items);
		}

		class ViewHolder {
			public TextView label;
		}

		@Override
		protected int getItemLayoutId() {
			return R.layout.main_item;
		}

		@Override
		protected ViewHolder createHolder(View convertView) {
			ViewHolder holder = new ViewHolder();
			holder.label = (TextView)convertView.findViewById(android.R.id.text1);
			return holder;
		}

		@Override
		protected void bindView(ViewHolder holder, MainItem currentItem, View convertView) {
			holder.label.setText(currentItem.title);
		}
	}
}
