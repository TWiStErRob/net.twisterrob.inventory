package net.twisterrob.inventory.android.view;

import android.app.AlertDialog.Builder;
import android.content.*;
import android.database.Cursor;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;

import net.twisterrob.android.utils.tools.AndroidTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.activity.data.CategoryActivity;
import net.twisterrob.inventory.android.content.Loaders;
import net.twisterrob.inventory.android.content.contract.CommonColumns;
import net.twisterrob.inventory.android.content.model.*;
import net.twisterrob.inventory.android.fragment.BaseFragment;
import net.twisterrob.inventory.android.view.ChangeTypeListener.ChangeTypeDialog.Variants;
import net.twisterrob.inventory.android.view.adapters.TypeAdapter;

public class ChangeTypeListener implements OnClickListener {
	private BaseFragment fragment;
	private final Context context;
	private final ImagedVariants variants;

	public ChangeTypeListener(BaseFragment fragment, ImagedDTO entity) {
		this.fragment = fragment;
		this.context = fragment.getActivity();
		this.variants = which(entity);
	}

	@Override public void onClick(View v) {
		new ChangeTypeDialog(fragment).show(variants, variants.entity.type, variants.entity.name);
	}

	public static class ChangeTypeDialog {
		private final BaseFragment fragment;
		private final Context context;
		ChangeTypeDialog(BaseFragment fragment) {
			this.fragment = fragment;
			this.context = fragment.getActivity();
		}

		void show(final Variants variants, final long initialType, final CharSequence name) {
			fragment.getLoaderManager().initLoader(variants.getTypesLoader().id(), null,
					new CursorSwapper(context, new TypeAdapter(context)) {
						@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
							super.onLoadFinished(loader, data);
							show(variants, adapter, initialType, name);
						}
					}
			);
		}

		void show(final Variants variants, final CursorAdapter adapter, long initialType, final CharSequence name) {
			int position = AndroidTools.findItemPosition(adapter, initialType);
			Builder dialog = new Builder(context)
					.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							Cursor cursor = (Cursor)adapter.getItem(which);
							long newType = cursor.getLong(cursor.getColumnIndex(CommonColumns.ID));
							String newTypeKey = cursor.getString(cursor.getColumnIndex(CommonColumns.NAME));
							variants.update(newType); // FIXME DB on UI
							dialog.dismiss();
							fragment.refresh();

							CharSequence newTypeName = AndroidTools.getText(context, newTypeKey);
							App.toastUser(context.getString(R.string.generic_location_change, name, newTypeName));
						}
					});
			variants.augment(dialog);
			dialog.create().show();
		}

		public static abstract class Variants {
			abstract void update(long newType);
			protected abstract CharSequence getTitle();
			abstract Loaders getTypesLoader();
			void augment(Builder dialog) {
				dialog.setTitle(getTitle());
			}
		}
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
	}

	private class PropertyVariants extends ImagedVariants {
		@Override public void update(long newType) {
			PropertyDTO property = PropertyDTO.fromCursor(App.db().getProperty(entity.id));
			App.db().updateProperty(property.id, newType, property.name, property.description);
		}
		@Override public CharSequence getTitle() {
			return "Change Type of " + entity.name;
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.PropertyTypes;
		}
	}

	private class RoomVariants extends ImagedVariants {
		@Override public void update(long newType) {
			RoomDTO room = RoomDTO.fromCursor(App.db().getRoom(entity.id));
			App.db().updateRoom(room.id, newType, room.name, room.description);
		}
		@Override public CharSequence getTitle() {
			return "Change Type of " + entity.name;
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.RoomTypes;
		}
	}

	private class ItemVariants extends ImagedVariants {
		@Override public void update(long newType) {
			ItemDTO item = ItemDTO.fromCursor(App.db().getItem(entity.id, false));
			App.db().updateItem(item.id, newType, item.name, item.description);
		}
		@Override public CharSequence getTitle() {
			return "Change Category of " + entity.name;
		}
		@Override public Loaders getTypesLoader() {
			return Loaders.ItemCategories;
		}
		@Override void augment(Builder dialog) {
			super.augment(dialog);
			dialog.setNeutralButton("Jump to Category", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface dialog, int which) {
					context.startActivity(CategoryActivity.show(entity.type));
				}
			});
		}
	}
}
