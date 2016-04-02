package net.twisterrob.inventory.android.view;

import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.view.View.OnClickListener;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.*;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
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

		public void notifyUserOfUpdate(Cursor cursor) {
			String newTypeKey = cursor.getString(cursor.getColumnIndex(CommonColumns.NAME));
			CharSequence newTypeName = AndroidTools.getText(context, newTypeKey);
			App.toastUser(context.getString(R.string.generic_location_change, getName(), newTypeName));
			fragment.refresh();
		}
	}

	private class PropertyVariants extends ImagedVariants {
		@Override public void update(long newType, Cursor cursor) {
			PropertyDTO property = DatabaseDTOTools.retrieveProperty(entity.id);
			App.db().updateProperty(property.id, newType, property.name, property.description);
			super.notifyUserOfUpdate(cursor);
		}
		@Override public CharSequence getTitle() {
			return "Change Type of " + getName();
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.PropertyTypes;
		}
	}

	private class RoomVariants extends ImagedVariants {
		@Override public void update(long newType, Cursor cursor) {
			RoomDTO room = DatabaseDTOTools.retrieveRoom(entity.id);
			App.db().updateRoom(room.id, newType, room.name, room.description);
			super.notifyUserOfUpdate(cursor);
		}
		@Override public CharSequence getTitle() {
			return "Change Type of " + getName();
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.RoomTypes;
		}
	}

	private class ItemVariants extends ImagedVariants {
		@Override public void update(long newType, Cursor cursor) {
			ItemDTO item = DatabaseDTOTools.retrieveItem(entity.id);
			App.db().updateItem(item.id, newType, item.name, item.description);
			super.notifyUserOfUpdate(cursor);
		}
		@Override public CharSequence getTitle() {
			return "Change Category of " + getName();
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.ItemCategories;
		}
		@Override protected Bundle createArgs(long type) {
			return Intents.bundleFromCategory(type);
		}
		@Override protected void augment(Builder dialog) {
			super.augment(dialog);
			dialog.setNeutralButton("Jump to Category", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					context.startActivity(CategoryActivity.show(entity.type));
				}
			});
		}
	}
}
