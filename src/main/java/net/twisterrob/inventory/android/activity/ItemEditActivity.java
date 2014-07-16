package net.twisterrob.inventory.android.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;

import net.twisterrob.android.content.loader.*;
import net.twisterrob.android.content.loader.DynamicLoaderManager.Dependency;
import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.content.LoadSingleRow;
import net.twisterrob.inventory.android.content.contract.*;

import static net.twisterrob.inventory.android.content.Loaders.*;
public class ItemEditActivity extends BaseEditActivity {
	private EditText itemName;
	private TextView itemCategory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		super.setContentView(R.layout.item_edit);
		itemName = (EditText)findViewById(R.id.itemName);
		itemCategory = (TextView)findViewById(R.id.itemCategory);

		DynamicLoaderManager manager = new DynamicLoaderManager(getSupportLoaderManager());
		Bundle args = getIntent().getExtras();
		Dependency<Cursor> loadItemData = manager.add(SingleItem.ordinal(), args, new LoadExistingItem());
		Dependency<Void> loadItemCondition = manager.add(-SingleItem.ordinal(), args, new IsExistingItem());

		loadItemData.dependsOn(loadItemCondition);
		manager.startLoading();
	}

	private final class IsExistingItem extends DynamicLoaderManager.Condition {
		private IsExistingItem() {
			super(ItemEditActivity.this);
		}

		@Override
		protected boolean test(int id, Bundle args) {
			return args != null && args.getLong(Extras.ITEM_ID, Item.ID_ADD) != Item.ID_ADD;
		}
	}

	private final class LoadExistingItem extends LoadSingleRow {
		private LoadExistingItem() {
			super(ItemEditActivity.this);
		}

		@Override
		protected void process(Cursor item) {
			super.process(item);
			String name = item.getString(item.getColumnIndex(Item.NAME));
			String category = item.getString(item.getColumnIndex(Item.CATEGORY));

			setTitle(name);
			itemName.setText(name);
			itemCategory.setText(category);
		}

		@Override
		protected void processInvalid(Cursor item) {
			super.processInvalid(item);
			finish();
		}
	}
}
