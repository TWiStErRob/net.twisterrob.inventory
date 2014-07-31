package net.twisterrob.inventory.android.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.activity.*;
import net.twisterrob.inventory.android.content.LoadSingleRow;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;
import net.twisterrob.inventory.android.fragment.ItemViewFragment.ItemEvents;
import net.twisterrob.inventory.android.tasks.DeleteItemTask;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemViewFragment extends BaseEditFragment<ItemEvents> {
	public interface ItemEvents {
		void itemLoaded(ItemDTO item);
	}

	public ItemViewFragment() {
		setDynamicResource(DYN_EventsClass, ItemEvents.class);
		setDynamicResource(DYN_OptionsMenu, R.menu.item);
	}

	private TextView itemName;
	private TextView itemCategory;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.item_view, container, false);
		itemName = (TextView)root.findViewById(R.id.itemName);
		itemCategory = (TextView)root.findViewById(R.id.itemCategory);
		return root;
	}

	@Override
	protected void onStartLoading() {
		Bundle args = new Bundle();
		args.putLong(Extras.ITEM_ID, getArgItemID());
		getLoaderManager().initLoader(SingleItem.ordinal(), args, new LoadExistingItem());
	}

	public void refresh() {
		getLoaderManager().getLoader(SingleItem.ordinal()).forceLoad();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_edit:
				startActivity(ItemEditActivity.edit(getArgItemID()));
				return true;
			case R.id.action_item_delete:
				Dialogs.executeTask(getActivity(), new DeleteItemTask(getArgItemID(), new Dialogs.Callback() {
					public void success() {
						getActivity().finish();
					}
					public void failed() {
						String message = "This property still has some items";
						Toast.makeText(App.getAppContext(), message, Toast.LENGTH_LONG).show();
					}
				}));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private long getArgItemID() {
		return getArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	private final class LoadExistingItem extends LoadSingleRow {
		private LoadExistingItem() {
			super(getActivity());
		}

		@Override
		protected void process(Cursor cursor) {
			super.process(cursor);
			ItemDTO item = ItemDTO.fromCursor(cursor);

			getActivity().setTitle(item.name);
			itemName.setText(item.name);
			itemCategory.setText(String.valueOf(item.category));

			eventsListener.itemLoaded(item);
		}
		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}

	public static ItemViewFragment newInstance(long itemID) {
		if (itemID == Item.ID_ADD) {
			throw new IllegalArgumentException("Must be an existing item");
		}

		ItemViewFragment fragment = new ItemViewFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.ITEM_ID, itemID);

		fragment.setArguments(args);
		return fragment;
	}
}
