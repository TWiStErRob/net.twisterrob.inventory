package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.example.android.xmladapters.Adapters;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.Extras;
import net.twisterrob.inventory.android.view.CursorSwapper;

public class ItemsFragment extends ListFragment {
	private static final Logger LOG = LoggerFactory.getLogger(ItemsFragment.class);

	public interface ItemEvents {
		void newItem();
		void itemSelected(long itemID);
		void itemActioned(long itemID);
	}

	private CursorAdapter adapter;
	private ListView list;
	private ItemEvents listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = checkActivityInterface(activity, ItemEvents.class);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
		super.onCreateOptionsMenu(menu, menuInflater);
		menuInflater.inflate(R.menu.items, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_add:
				listener.newItem();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.item_coll, container, false);
		list = (ListView)root.findViewById(R.id.items);
		adapter = Adapters.loadCursorAdapter(getActivity(), R.xml.items, (Cursor)null);

		list.setAdapter(adapter);

		return root;
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);

		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Long Clicked on #{}", id);
				listener.itemActioned(id);
				return true;
			}
		});
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LOG.trace("Clicked on #{}", id);
				listener.itemSelected(id);
			}
		});
	}

	public void list(long parentItemID) {
		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, parentItemID);
		getLoaderManager().initLoader(Loaders.Items.ordinal(), args, new CursorSwapper(getActivity(), adapter));
	}

	public void refresh() {
		getLoaderManager().getLoader(Loaders.Items.ordinal()).forceLoad();
	}
}
