package net.twisterrob.inventory.android.view;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.*;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity.Builder;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.ChangeTypeDialog.Variants;

public class ItemSelectionActionMode extends SelectionActionMode {
	private static final int PICK_REQUEST = 1;

	private final BaseFragment<?> fragment;
	private final Builder builder;

	public ItemSelectionActionMode(BaseFragment<?> fragment, SelectionAdapter<?> adapter, Builder builder) {
		super(fragment.getActivity(), adapter);
		this.fragment = fragment;
		this.builder = builder;
	}

	@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.item_bulk, menu);
		return super.onCreateActionMode(mode, menu);
	}

	@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_item_delete:
				delete(getSelectedIDs());
				return true;
			case R.id.action_item_move:
				Intent intent = builder.resetForbidItems()
				                       .forbidItems(getSelectedIDs())
				                       .build();
				fragment.startActivityForResult(intent, PICK_REQUEST);
				return true;
			case R.id.action_item_categorize:
				final long[] itemIDs = getSelectedIDs();
				long category = App.db().findCommonCategory(itemIDs);
				new ChangeTypeDialog(fragment).show(new Variants() {
					@SuppressWarnings("WrongThread") // FIXME DB on UI
					@Override protected void update(Cursor cursor) {
						long newType = DatabaseTools.getLong(cursor, Item.ID);
						for (long itemID : itemIDs) {
							ItemDTO item = DatabaseDTOTools.retrieveItem(itemID);
							App.db().updateItem(item.id, newType, item.name, item.description);
						}
						String newTypeKey = DatabaseTools.getString(cursor, CommonColumns.NAME);
						CharSequence newTypeName = AndroidTools.getText(fragment.getContext(), newTypeKey);
						App.toastUser(fragment.getContext()
						                      .getString(R.string.generic_location_change, "selection", newTypeName));
						finish();
						fragment.refresh();
					}
					@Override protected CharSequence getTitle() {
						return "Change Category";
					}
					@Override protected Loaders getTypesLoader() {
						return Loaders.ItemCategories;
					}
					@Override protected Bundle createArgs(long type) {
						return Intents.bundleFromCategory(type);
					}
					@Override protected CharSequence getName() {
						return "selection";
					}
					@Override protected boolean isExpandable() {
						return true;
					}
					@Override public CharSequence getTypeName(Cursor cursor) {
						long categoryID = DatabaseTools.getLong(cursor, Category.ID);
						CategoryCache cache = CategoryDTO.getCache(fragment.getContext());
						return cache.getCategoryPath(categoryID);
					}
					@Override public CharSequence getKeywords(Cursor cursor) {
						long categoryID = DatabaseTools.getLong(cursor, Category.ID);
						CategoryCache cache = CategoryDTO.getCache(fragment.getContext());
						return CategoryDTO.getKeywords(fragment.getContext(), cache.getCategoryKey(categoryID), true);
					}
				}, category);
				return true;
		}
		return super.onActionItemClicked(mode, item);
	}

	@Override public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_REQUEST) {
			switch (resultCode) {
				case MoveTargetActivity.ROOM: {
					long roomID = data.getLongExtra(Extras.ROOM_ID, Room.ID_ADD);
					moveToRoom(roomID, getSelectedIDs());
					return true;
				}
				case MoveTargetActivity.ITEM: {
					long parentID = data.getLongExtra(Extras.ITEM_ID, Item.ID_ADD);
					move(parentID, getSelectedIDs());
					return true;
				}
			}
		}
		return false;
	}

	private void delete(final long... itemIDs) {
		Dialogs.executeConfirm(getActivity(), new DeleteItemsAction(itemIDs) {
			public void finished() {
				finish();
				fragment.refresh();
			}
		});
	}

	private void moveToRoom(final long roomID, final long... itemIDs) {
		Dialogs.executeDirect(getActivity(), new MoveItemsToRoomAction(roomID, itemIDs) {
			public void finished() {
				finish();
				fragment.refresh();
			}
			@Override public void undoFinished() {
				fragment.refresh();
			}
		});
	}

	private void move(final long parentID, final long... itemIDs) {
		Dialogs.executeDirect(getActivity(), new MoveItemsAction(parentID, itemIDs) {
			public void finished() {
				finish();
				fragment.refresh();
			}
			@Override public void undoFinished() {
				fragment.refresh();
			}
		});
	}
}
