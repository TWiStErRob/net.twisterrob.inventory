package net.twisterrob.inventory.android.view;

import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.view.View.OnClickListener;

import net.twisterrob.android.utils.concurrent.ClosureAsyncTask;
import net.twisterrob.android.utils.tools.*;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.*;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.ChangeTypeDialog.Variants;

public class ChangeTypeListener implements OnClickListener {
	private final BaseFragment<?> fragment;
	private final Context context;
	private final ImagedVariants variants;

	public ChangeTypeListener(BaseFragment<?> fragment, ImagedDTO entity) {
		this.fragment = fragment;
		this.context = fragment.getActivity();
		this.variants = which(entity);
	}

	@Override public void onClick(View v) {
		new ChangeTypeDialog(fragment).show(variants, variants.entity.type);
	}

	private ImagedVariants which(ImagedDTO entity) {
		ImagedVariants variants;
		if (entity instanceof ItemDTO) {
			variants = new ItemVariants();
		} else if (entity instanceof RoomDTO) {
			variants = new RoomVariants();
		} else if (entity instanceof PropertyDTO) {
			variants = new PropertyVariants();
		} else {
			throw new UnsupportedOperationException("Cannot handle " + entity);
		}
		variants.entity = entity;
		return variants;
	}

	private abstract class ImagedVariants extends Variants {
		ImagedDTO entity;

		@Override protected CharSequence getName() {
			return entity.name;
		}

		@Override protected final void update(Cursor cursor) {
			// prefetch, because the cursor may re-position once this method returns
			final long newType = DatabaseTools.getLong(cursor, CommonColumns.ID);
			final String newTypeKey = DatabaseTools.getString(cursor, CommonColumns.NAME);
			AndroidTools.executePreferSerial(new ClosureAsyncTask() {
				@Override protected void doInBackgroundSafe() {
					doUpdate(newType);
				}
				@Override protected void onResult() {
					CharSequence newTypeName = ResourceTools.getText(context, newTypeKey);
					App.toastUser(context.getString(R.string.generic_location_change, getName(), newTypeName));
					fragment.refresh();
				}
				@Override protected void onError(@NonNull Exception ex) {
					App.toastUser(App.getError(ex, "Cannot change type"));
				}
			});
		}
		@WorkerThread
		protected abstract void doUpdate(long newType);
	}

	private class PropertyVariants extends ImagedVariants {
		@Override public void doUpdate(long newType) {
			PropertyDTO property = DatabaseDTOTools.retrieveProperty(entity.id);
			App.db().updateProperty(property.id, newType, property.name, property.description);
		}
		@Override public CharSequence getTitle() {
			return context.getString(R.string.property_change_type, getName());
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.PropertyTypes;
		}
		@Override protected boolean isExpandable() {
			return false;
		}
		@Override public CharSequence getTypeName(Cursor cursor) {
			String propertyType = DatabaseTools.getString(cursor, PropertyType.NAME);
			return ResourceTools.getText(context, propertyType);
		}
		@Override public CharSequence getKeywords(Cursor cursor) {
			String propertyType = DatabaseTools.getString(cursor, PropertyType.NAME);
			return ResourceTools.getText(context, ResourceNames.getKeywordsName(propertyType));
		}
	}

	private class RoomVariants extends ImagedVariants {
		@Override public void doUpdate(long newType) {
			RoomDTO room = DatabaseDTOTools.retrieveRoom(entity.id);
			App.db().updateRoom(room.id, newType, room.name, room.description);
		}
		@Override public CharSequence getTitle() {
			return context.getString(R.string.room_change_type, getName());
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.RoomTypes;
		}
		@Override protected boolean isExpandable() {
			return false;
		}
		@Override public CharSequence getTypeName(Cursor cursor) {
			String roomType = DatabaseTools.getString(cursor, RoomType.NAME);
			return ResourceTools.getText(context, roomType);
		}
		@Override public CharSequence getKeywords(Cursor cursor) {
			String roomType = DatabaseTools.getString(cursor, RoomType.NAME);
			return ResourceTools.getText(context, ResourceNames.getKeywordsName(roomType));
		}
	}

	private class ItemVariants extends ImagedVariants {
		@Override public void doUpdate(long newType) {
			ItemDTO item = DatabaseDTOTools.retrieveItem(entity.id);
			App.db().updateItem(item.id, newType, item.name, item.description);
		}
		@Override public CharSequence getTitle() {
			return context.getString(R.string.item_categorize_title, getName());
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.ItemCategories;
		}
		@Override protected Bundle createArgs(long type) {
			return Intents.bundleFromCategory(type);
		}
		@Override protected void augment(Builder dialog) {
			super.augment(dialog);
			dialog.setNeutralButton(R.string.category_goto, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					context.startActivity(CategoryActivity.show(entity.type));
				}
			});
		}
		@Override protected boolean isExpandable() {
			return true;
		}
		@Override public CharSequence getTypeName(Cursor cursor) {
			long categoryID = DatabaseTools.getLong(cursor, Category.ID);
			CategoryCache cache = CategoryDTO.getCache(context);
			return cache.getCategoryPath(categoryID);
		}
		@Override public CharSequence getKeywords(Cursor cursor) {
			long categoryID = DatabaseTools.getLong(cursor, Category.ID);
			CategoryCache cache = CategoryDTO.getCache(context);
			return CategoryDTO.getKeywords(context, cache.getCategoryKey(categoryID), true);
		}
	}
}
