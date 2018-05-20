package net.twisterrob.inventory.android.activity.data;

import android.annotation.SuppressLint;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;

import net.twisterrob.android.activity.AboutActivity;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.ListDTO;
import net.twisterrob.inventory.android.fragment.ListViewFragment.ListEvents;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment;
import net.twisterrob.inventory.android.fragment.data.ItemListFragment.ItemsEvents;

public class ListItemsActivity extends BaseDetailActivity<ItemListFragment> implements ItemsEvents, ListEvents {
	public ListItemsActivity() {
		super(R.plurals.list);
	}

	@Override protected ItemListFragment onCreateFragment() {
		return ItemListFragment.newListInstance(getExtraListID()).addHeader(null);
	}

	@Override public void listLoaded(ListDTO list) {
		setActionBarTitle(list.name);
	}

	@Override public void listDeleted(ListDTO list) {
		finish();
	}

	public void itemSelected(long itemID) {
		startActivity(ItemViewActivity.show(itemID));
	}

	public void itemActioned(long itemID) {
		startActivity(ItemEditActivity.edit(itemID));
	}

	public void newItem(long parentID) {
		AlertDialog dialog = new Builder(this)
				.setTitle(R.string.list_add_item_title)
				.setMessage(getString(R.string.list_add_item_description, getString(R.string.list_manage)))
				.setPositiveButton(android.R.string.ok, null)
				.setNeutralButton(R.string.action_read_more, new OnClickListener() {
					@Override public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(getApplicationContext(), AboutActivity.class));
					}
				})
				.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	@Override public void onContentChanged() {
		super.onContentChanged();
		setupTitleEditor();
	}

	@SuppressLint({"WrongThread", "WrongThreadInterprocedural"}) // FIXME DB on UI
	@Override protected void updateName(String newName) {
		App.db().updateList(getExtraListID(), newName);
	}

	private long getExtraListID() {
		return getIntent().getLongExtra(Extras.LIST_ID, CommonColumns.ID_ADD);
	}

	public static Intent show(long listID) {
		Intent intent = new Intent(App.getAppContext(), ListItemsActivity.class);
		intent.putExtra(Extras.LIST_ID, listID);
		return intent;
	}
}
