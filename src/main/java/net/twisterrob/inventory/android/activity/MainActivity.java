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
import net.twisterrob.inventory.android.content.contract.Extras;

public class MainActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.main);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);

		final GridView list = (GridView)findViewById(android.R.id.list);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MainItem item = (MainItem)parent.getAdapter().getItem(position);
				item.listener.onClick(view);
			}
		});
		Collection<MainItem> actions = Arrays.asList( //
				new MainItem("Properties", new OnClickListener() {
					public void onClick(View v) {
						Intent intent = createIntent(PropertiesActivity.class);
						startActivity(intent);
					}
				}), new MainItem("Edit Item #5", new OnClickListener() {
					public void onClick(View v) {
						Intent intent = createIntent(ItemEditActivity.class);
						intent.putExtra(Extras.ITEM_ID, 5L);
						startActivity(intent);
					}
				}), new MainItem("Drive", new OnClickListener() {
					public void onClick(View v) {
						Intent intent = createIntent(WelcomeActivity.class);
						startActivity(intent);
					}
				}));
		list.setAdapter(new MainItemAdapter(this, actions));

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				((MainItem)list.getItemAtPosition(2)).listener.onClick(list);
				return null;
			}
		}.execute();
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
