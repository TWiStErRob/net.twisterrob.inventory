package net.twisterrob.inventory.android.activity;

import android.content.Intent;
import android.os.Bundle;

import net.twisterrob.inventory.R;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.fragment.ItemEditFragment;
public class ItemEditActivity extends BaseEditActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.item);

		ItemEditFragment editor = getFragment(R.id.item);
		editor.load(getIntent().getLongExtra(Extras.ITEM_ID, Item.ID_ADD));
	}

	public static Intent add() {
		Intent intent = new Intent(App.getAppContext(), ItemEditActivity.class);
		return intent;
	}

	public static Intent edit(long itemID) {
		Intent intent = new Intent(App.getAppContext(), ItemEditActivity.class);
		intent.putExtra(Extras.ITEM_ID, itemID);
		return intent;
	}
}
