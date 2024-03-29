package net.twisterrob.inventory.android.view;

import android.annotation.SuppressLint;
import android.content.*;
import android.database.Cursor;
import android.os.*;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;

import net.twisterrob.android.utils.tools.*;
import net.twisterrob.android.view.SelectionAdapter;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.MoveTargetActivity;
import net.twisterrob.inventory.android.categories.cache.CategoryCache;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.tasks.*;
import net.twisterrob.inventory.android.view.ChangeTypeDialog.Variants;

public class ItemSelectionActionMode extends SelectionActionMode {
	private static final int PICK_REQUEST = 1;

	private final @NonNull BaseFragment<?> fragment;
	private final @NonNull MoveTargetActivity.Builder builder;
	private final @NonNull CategoryVisuals visuals;
	private final @NonNull CategoryCache cache;

	public ItemSelectionActionMode(
			@NonNull BaseFragment<?> fragment,
			@NonNull SelectionAdapter<?> adapter,
			@NonNull CategoryVisuals visuals,
			@NonNull CategoryCache cache,
			@NonNull MoveTargetActivity.Builder builder
	) {
		super(fragment.requireActivity(), adapter);
		this.fragment = PreconditionsKt.checkNotNull(fragment);
		this.visuals = PreconditionsKt.checkNotNull(visuals);
		this.cache = PreconditionsKt.checkNotNull(cache);
		this.builder = PreconditionsKt.checkNotNull(builder);
	}

	@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		mode.getMenuInflater().inflate(R.menu.item_bulk, menu);
		return super.onCreateActionMode(mode, menu);
	}

	@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_item_delete) {
			delete(getSelectedIDs());
			return true;
		} else if (id == R.id.action_item_move) {
			Intent intent = builder.resetForbidItems()
			                       .forbidItems(getSelectedIDs())
			                       .build();
			fragment.startActivityForResult(intent, PICK_REQUEST);
			return true;
		} else if (id == R.id.action_item_categorize) {
			final long[] itemIDs = getSelectedIDs();
			long category;
			StrictMode.ThreadPolicy originalPolicy = StrictMode.allowThreadDiskReads();
			try {
				category = App.db().findCommonCategory(itemIDs);
			} finally {
				StrictMode.setThreadPolicy(originalPolicy);
			}
			new ChangeTypeDialog(fragment).show(new Variants() {
				@SuppressLint({"WrongThread", "WrongThreadInterprocedural"}) // FIXME DB on UI
				@Override protected void update(Cursor cursor) {
					long newType = DatabaseTools.getLong(cursor, Item.ID);
					for (long itemID : itemIDs) {
						ItemDTO item = DatabaseDTOTools.retrieveItem(itemID);
						App.db().updateItem(item.id, newType, item.name, item.description);
					}
					String newTypeKey = DatabaseTools.getString(cursor, CommonColumns.NAME);
					Context context = fragment.requireContext();
					CharSequence newTypeName = ResourceTools.getText(context, newTypeKey);
					App.toastUser(context.getString(R.string.generic_location_change, "selection", newTypeName));
					finish();
					fragment.refresh();
				}
				@Override protected CharSequence getTitle() {
					return fragment.getResources().getString(R.string.item_categorize_title_many);
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
					return cache.getCategoryPath(categoryID);
				}
				@Override public CharSequence getKeywords(Cursor cursor) {
					long categoryID = DatabaseTools.getLong(cursor, Category.ID);
					String categoryKey = cache.getCategoryKey(categoryID);
					return visuals.getKeywords(categoryKey, true);
				}
			}, category);
			return true;
		} else {
			return super.onActionItemClicked(mode, item);
		}
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
