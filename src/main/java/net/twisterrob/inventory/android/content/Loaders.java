package net.twisterrob.inventory.android.content;

import org.slf4j.*;

import android.app.SearchManager;
import android.content.Context;
import android.database.*;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import net.twisterrob.android.content.loader.SimpleCursorLoader;
import net.twisterrob.inventory.android.App;
import net.twisterrob.inventory.android.content.contract.*;

public enum Loaders {
	PropertyTypes {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			return App.db().listPropertyTypes();
		}
	},
	Properties {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			return App.db().listProperties();
		}
	},
	SingleProperty {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			long id = args.getLong(Extras.PROPERTY_ID, Property.ID_ADD);
			return App.db().getProperty(id);
		}
	},
	RoomTypes {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			return App.db().listRoomTypes();
		}
	},
	Rooms {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			long id = args.getLong(Extras.PROPERTY_ID, Property.ID_ADD);
			if (id == Property.ID_ADD) {
				return App.db().listRooms();
			} else {
				return App.db().listRooms(id);
			}
		}
	},
	SingleRoom {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			long id = args.getLong(Extras.ROOM_ID, Room.ID_ADD);
			return App.db().getRoom(id);
		}
	},
	ItemCategories {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			return App.db().listItemCategories();
		}
	},
	Items {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			if (args.containsKey(Extras.PARENT_ID)) {
				long id = args.getLong(Extras.PARENT_ID);
				return App.db().listItems(id);
			}
			if (args.containsKey(Extras.ROOM_ID)) {
				long roomID = args.getLong(Extras.ROOM_ID);
				return App.db().listItemsInRoom(roomID);
			}
			if (args.containsKey(Extras.LIST_ID)) {
				long listID = args.getLong(Extras.LIST_ID);
				return App.db().listItemsInList(listID);
			}
			if (args.containsKey(Extras.CATEGORY_ID)) {
				long catID = args.getLong(Extras.CATEGORY_ID);
				boolean include = args.getBoolean(Extras.INCLUDE_SUBS, false);
				return App.db().listItemsForCategory(catID, include);
			}
			return App.db().listItems();
		}
	},
	SingleItem {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			long id = args.getLong(Extras.ITEM_ID, Item.ID_ADD);
			return App.db().getItem(id);
		}
	},
	Categories {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			Long id = (Long)args.get(Extras.CATEGORY_ID);
			return App.db().listCategories(id);
		}
	},
	SingleCategory {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			long id = args.getLong(Extras.CATEGORY_ID, Category.ID_ADD);
			return App.db().getCategory(id);
		}
	},
	ItemParents {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			long id = args.getLong(Extras.ITEM_ID, Item.ID_ADD);
			return App.db().listItemParents(id);
		}
	},
	ItemSearch {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			CharSequence query = args.getCharSequence(SearchManager.QUERY);
			return InventoryDatabase.getInstance().searchItems(context.getContentResolver(), query);
		}
	},
	SingleList {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			long id = args.getLong(Extras.LIST_ID, CommonColumns.ID_ADD);
			return App.db().getList(id);
		}
	},
	Lists {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			long id = args.getLong(Extras.ITEM_ID, Item.ID_ADD);
			return App.db().listLists(id);
		}
	},
	Recents {
		@Override
		protected Cursor createCursor(Context context, Bundle args) {
			return App.db().listRecents();
		}
	},;

	private static final Logger LOG = LoggerFactory.getLogger(Loaders.class);

	private static final Bundle NO_ARGS = new Bundle(0);

	protected abstract Cursor createCursor(Context context, Bundle args);

	public Loader<Cursor> createLoader(Context context, final Bundle args) {
		return new SimpleCursorLoader(context) {
			@Override
			public Cursor loadInBackground() {
				LOG.trace("Loading {}({})", Loaders.this, args);
				Cursor cursor = createCursor(getContext(), args != null? args : NO_ARGS);
				if (args == null || !args.getBoolean("dontExecute")) {
					cursor.getCount();
				}
				return cursor;
			}
			@Override
			public void deliverResult(Cursor cursor) {
				try {
					super.deliverResult(cursor);
				} catch (RuntimeException ex) {
					try {
						DatabaseUtils.dumpCursor(cursor);
					} catch (Exception dumpEx) {
						dumpEx.printStackTrace();
					}
					throw ex;
				}
			}
		};
	}

	public static Loaders fromID(int id) {
		return values()[id];
	}

	public abstract static class LoadersCallbacks implements LoaderCallbacks<Cursor> {
		protected final Context context;

		public LoadersCallbacks(Context context) {
			this.context = context;
		}

		@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return fromID(id).createLoader(context, args);
		}
	}
}
