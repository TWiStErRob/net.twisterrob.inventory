package net.twisterrob.inventory.android.fragment;

import org.slf4j.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.android.utils.concurrent.SimpleAsyncTask;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.ItemDTO;

import static net.twisterrob.inventory.android.content.Loaders.*;

public class ItemEditFragment extends BaseEditFragment<Void> {
	private static final Logger LOG = LoggerFactory.getLogger(ItemEditFragment.class);

	private EditText itemName;
	private TextView itemCategory;
	private ImageView itemImage;

	public ItemEditFragment() {
		setDynamicResource(DYN_ImageView, R.id.itemImage);
	}

	@Override
	protected String getBaseFileName() {
		return "Item_" + getArgItemID();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.item_edit, container, false);
		itemName = (EditText)root.findViewById(R.id.itemName);
		itemCategory = (TextView)root.findViewById(R.id.itemCategory);
		itemImage = (ImageView)root.findViewById(R.id.itemImage);

		((Button)root.findViewById(R.id.btn_save)).setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				save();
			}
		});

		return root;
	}

	@Override
	protected void onStartLoading() {
		long id = getArgItemID();

		DynamicLoaderManager manager = new DynamicLoaderManager(getLoaderManager());

		if (id != Item.ID_ADD) {
			Bundle args = new Bundle();
			args.putLong(Extras.ITEM_ID, id);
			@SuppressWarnings("unused")
			// no dependencies yet: Item's category will come in
			Dependency<Cursor> loadItemData = manager.add(SingleItem.ordinal(), args, new LoadExistingItem());
		}

		manager.startLoading();
	}

	private void save() {
		new SaveTask().execute(getCurrentItem());
	}

	private ItemDTO getCurrentItem() {
		ItemDTO item = new ItemDTO();
		item.parentID = getArgParentID();
		item.id = getArgItemID();
		item.name = itemName.getText().toString();
		// item.category = itemCategory.getSelectedItemId(); // TODO tree ListView?
		return item;
	}

	private long getArgItemID() {
		return getArguments().getLong(Extras.ITEM_ID, Item.ID_ADD);
	}

	private long getArgParentID() {
		return getArguments().getLong(Extras.PARENT_ID, Item.ID_ADD);
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
			itemImage.setImageResource(R.drawable.category_unknown);
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			getActivity().finish();
		}
	}

	private final class SaveTask extends SimpleAsyncTask<ItemDTO, Void, Long> {
		@Override
		protected Long doInBackground(ItemDTO param) {
			try {
				Database db = App.db();
				if (param.id == Item.ID_ADD) {
					return db.newItem(param.parentID, param.name, param.category);
				} else {
					db.updateItem(param.id, param.name, param.category);
					return param.id;
				}
			} catch (SQLiteConstraintException ex) {
				LOG.warn("Cannot save {}", param, ex);
				return null;
			}
		}

		@Override
		protected void onPostExecute(Long result) {
			if (result != null) {
				getActivity().finish();
			} else {
				Toast.makeText(getActivity(), "Item name must be unique within the item collection", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public static ItemEditFragment newInstance(long parentID, long itemID) {
		if (parentID == Item.ID_ADD && itemID == Item.ID_ADD) {
			throw new IllegalArgumentException("Parent item ID / item ID must be provided (new item / edit item)");
		}
		if (itemID != Item.ID_ADD) { // no need to know which parent when editing
			parentID = Item.ID_ADD;
		}

		ItemEditFragment fragment = new ItemEditFragment();

		Bundle args = new Bundle();
		args.putLong(Extras.PARENT_ID, parentID);
		args.putLong(Extras.ITEM_ID, itemID);

		fragment.setArguments(args);
		return fragment;
	}
}
