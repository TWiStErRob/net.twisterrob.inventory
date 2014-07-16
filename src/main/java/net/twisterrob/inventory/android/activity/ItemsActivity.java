package net.twisterrob.inventory.android.activity;

import org.slf4j.*;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class ItemsActivity extends BaseListActivity {
	private static final Logger LOG = LoggerFactory.getLogger(ItemsActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.item_list);
		CursorAdapter adapter = Adapters.loadCursorAdapter(this, R.xml.items, (Cursor)null);

		Bundle args = new Bundle();
		long itemID = getIntent().getLongExtra(Extras.PARENT_ID, Item.ID_ADD);
		args.putLong(Extras.PARENT_ID, itemID);

		getSupportLoaderManager().initLoader(Loaders.Items.ordinal(), args, new CursorSwapper(this, adapter));

		ListView items = (ListView)findViewById(R.id.items);
		items.setAdapter(adapter);

		items.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				Intent intent = createIntent(ItemEditActivity.class);
				intent.putExtra(Extras.ITEM_ID, id);
				startActivity(intent);
				return true;
			}
		});
		items.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				if (id == Item.ID_ADD) {
					Intent intent = createIntent(ItemEditActivity.class);
					startActivity(intent);
				} else {
					Intent intent = createIntent(ItemsActivity.class);
					intent.putExtra(Extras.PARENT_ID, id);
					startActivity(intent);
				}
			}
		});
	}
}
