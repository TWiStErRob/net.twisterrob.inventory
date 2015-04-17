package net.twisterrob.inventory.android.content;

import org.slf4j.*;

import android.app.SearchManager;
import android.content.Context;
import android.database.*;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

import net.twisterrob.android.content.loader.SimpleCursorLoader;
import net.twisterrob.android.utils.tools.AndroidTools;
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
			return App.db().getItem(id, true);
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
		return new LoadersCursorLoader(context, Loaders.this, args);
	}

	private static final int MOD = (int)Math.pow(10, Math.ceil(Math.log10(values().length)));
	private static final int INT_SIZE = (int)Math.pow(10, Math.floor(Math.log10(Integer.MAX_VALUE)));

	public int id() {
		return ordinal(); // === id(0)
	}

	public int id(int mixin) {
		return (int)((mixin * (long)MOD) % INT_SIZE) + ordinal();
	}

	public static Loaders fromID(int id) {
		return values()[(id % MOD + MOD) % MOD]; // cycle up if negative
	}

	public abstract static class LoadersCallbacks implements LoaderCallbacks<Cursor> {
		protected final Context context;

		public LoadersCallbacks(Context context) {
			this.context = context;
		}

		@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Loader<Cursor> loader = fromID(id).createLoader(context, args);
			//LOG.trace("Created {}", loader);
			return loader;
		}

		@Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			if (loader instanceof LoadersCursorLoader) {
				((LoadersCursorLoader)loader).timeLoadFinished = System.nanoTime();
			}
			//LOG.trace("Finished {}: {}", loader, data);
		}
		@Override public void onLoaderReset(Loader<Cursor> loader) {
			if (loader instanceof LoadersCursorLoader) {
				((LoadersCursorLoader)loader).timeLoaderReset = System.nanoTime();
			}
			//LOG.trace("Reset {}", loader);
		}
	}

	private static class LoadersCursorLoader extends SimpleCursorLoader {
		private final Loaders loaders;
		private final Bundle args;
		private long timeCreateLoader = System.nanoTime();
		private long timeForceLoad = timeCreateLoader;
		private long timeCursorWanted = timeCreateLoader; // BG entry
		private long timeCursorCreated = timeCreateLoader; // in BG
		private long timeCursorExecuted = timeCreateLoader; // in BG
		private long timeCursorDelivered = timeCreateLoader; // BG exit, on UI
		long timeLoadFinished = timeCreateLoader; // on UI
		long timeLoaderReset = timeCreateLoader; // on UI

		public LoadersCursorLoader(Context context, Loaders loaders, Bundle args) {
			super(context);
			this.loaders = loaders;
			this.args = args;
		}

		@Override
		public Cursor loadInBackground() {
			timeCursorWanted = System.nanoTime();
			Cursor cursor = loaders.createCursor(getContext(), args != null? args : NO_ARGS);
			timeCursorCreated = System.nanoTime();
			if (args == null || !args.getBoolean("dontExecute")) {
				cursor.getCount();
				timeCursorExecuted = System.nanoTime();
			}
			return cursor;
		}

		@Override
		public void deliverResult(Cursor cursor) {
			timeCursorDelivered = System.nanoTime();
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

		@Override public void forceLoad() {
			timeForceLoad = System.nanoTime();
			super.forceLoad();
		}

		@Override public String toString() {
			return super.toString() + "=" + loaders + "(" + AndroidTools.toString(args) + ")" + ","
					+ " [" + "loader " + (timeCreateLoader - timeForceLoad) / 1000 / 1000 + "ms"
					+ ", " + "wanted " + (timeCursorWanted - timeForceLoad) / 1000 / 1000 + "ms"
					+ ", " + "created " + (timeCursorCreated - timeForceLoad) / 1000 / 1000 + "ms"
					+ ", " + "executed " + (timeCursorExecuted - timeForceLoad) / 1000 / 1000 + "ms"
					+ ", " + "delivered " + (timeCursorDelivered - timeForceLoad) / 1000 / 1000 + "ms"
					+ ", " + "finished " + (timeLoadFinished - timeForceLoad) / 1000 / 1000 + "ms"
					+ ", " + "reset " + (timeLoaderReset - timeForceLoad) / 1000 / 1000 + "ms"
					+ "]"
					;
		}
	}
}
