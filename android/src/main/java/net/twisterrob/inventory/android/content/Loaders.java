package net.twisterrob.inventory.android.content;

import org.slf4j.*;

import android.app.SearchManager;
import android.content.Context;
import android.database.*;
import android.os.Bundle;

import androidx.annotation.*;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

import net.twisterrob.android.content.loader.SimpleCursorLoader;
import net.twisterrob.android.utils.tools.StringerTools;
import net.twisterrob.inventory.android.*;
import net.twisterrob.inventory.android.content.Intents.Extras;
import net.twisterrob.inventory.android.content.contract.*;

public enum Loaders implements InventoryLoader {
	PropertyTypes {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle ignore) {
			return App.db().listPropertyTypes();
		}
	},
	Properties {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle ignore) {
			return App.db().listProperties();
		}
	},
	SingleProperty {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			long id = args.getLong(Extras.PROPERTY_ID, Property.ID_ADD);
			return App.db().getProperty(id);
		}
	},
	RoomTypes {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle ignore) {
			return App.db().listRoomTypes();
		}
	},
	Rooms {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			long id = args.getLong(Extras.PROPERTY_ID, Property.ID_ADD);
			if (id == Property.ID_ADD) {
				return App.db().listRooms();
			} else {
				return App.db().listRooms(id);
			}
		}
	},
	SingleRoom {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			long id = args.getLong(Extras.ROOM_ID, Room.ID_ADD);
			return App.db().getRoom(id);
		}
	},
	ItemCategories {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			Long id = (Long)args.get(Extras.CATEGORY_ID);
			return App.db().listRelatedCategories(id);
		}
	},
	ItemCategoriesAll {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle ignore) {
			return App.db().listRelatedCategories(null);
		}
	},
	Items {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
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
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			long id = args.getLong(Extras.ITEM_ID, Item.ID_ADD);
			return App.db().getItem(id, true);
		}
	},
	Categories {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			Long id = (Long)args.get(Extras.CATEGORY_ID);
			return App.db().listCategories(id);
		}
	},
	SingleCategory {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			long id = args.getLong(Extras.CATEGORY_ID, Category.ID_ADD);
			return App.db().getCategory(id);
		}
	},
	ItemParents {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			long id = args.getLong(Extras.ITEM_ID, Item.ID_ADD);
			return App.db().listItemParents(id);
		}
	},
	ItemSearch {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			CharSequence query = args.getCharSequence(SearchManager.QUERY);
			return InventoryDatabase.getInstance().searchItems(context.getContentResolver(), query);
		}
	},
	SingleList {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			long id = args.getLong(Extras.LIST_ID, CommonColumns.ID_ADD);
			return App.db().getList(id);
		}
	},
	Lists {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args) {
			long id = args.getLong(Extras.ITEM_ID, Item.ID_ADD);
			return App.db().listLists(id);
		}
	},
	Recents {
		@Override protected @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle ignore) {
			return App.db().listRecents();
		}
	},;

	private static final Logger LOG = LoggerFactory.getLogger(Loaders.class);

	private static final Bundle NO_ARGS = new Bundle(0);

	protected abstract @NonNull Cursor createCursor(@NonNull Context context, @NonNull Bundle args);

	public Loader<Cursor> createLoader(@NonNull Context context, @Nullable Bundle args) {
		return new LoadersCursorLoader(context, Loaders.this, args);
	}

	@Override public LoaderCallbacks<Cursor> createCallbacks(Context context, final LoadersCallbacksListener listener) {
		return new LoadersCallbacks(context) {
			@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				listener.preOnCreateLoader(id, args);
				Loader<Cursor> loader = super.onCreateLoader(id, args);
				listener.postOnCreateLoader(loader, args);
				return loader;
			}
			@Override public void onLoadFinished(Loader<Cursor> loader, @Nullable Cursor data) {
				listener.preOnLoadFinished(loader, data);
				super.onLoadFinished(loader, data);
				listener.postOnLoadFinished(loader, data);
			}
			@Override public void onLoaderReset(Loader<Cursor> loader) {
				listener.preOnLoaderReset(loader);
				super.onLoaderReset(loader);
				listener.postOnLoaderReset(loader);
			}
		};
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

	private static abstract class LoadersCallbacks implements LoaderCallbacks<Cursor> {
		protected final Context context;

		public LoadersCallbacks(Context context) {
			this.context = context;
		}

		@Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Loader<Cursor> loader = fromID(id).createLoader(context, args);
			LOG.trace("{}.onCreateLoader({}, {}){}", loader, id, args, getTimings(loader));
			return loader;
		}

		@Override public void onLoadFinished(Loader<Cursor> loader, @Nullable Cursor data) {
			if (loader instanceof LoadersCursorLoader) {
				((LoadersCursorLoader)loader).timeLoadFinished = System.nanoTime();
			}
			LOG.trace("{}.onLoadFinished({}){}", loader, data, getTimings(loader));
		}

		@Override public void onLoaderReset(Loader<Cursor> loader) {
			if (loader instanceof LoadersCursorLoader) {
				((LoadersCursorLoader)loader).timeLoaderReset = System.nanoTime();
			}
			LOG.trace("{}.onLoaderReset(){}", loader, getTimings(loader));
		}

		private String getTimings(Loader<Cursor> loader) {
			if (BuildConfig.DEBUG && loader instanceof LoadersCursorLoader) {
				LoadersCursorLoader l = ((LoadersCursorLoader)loader);
				return "\n"
						+ "[" + "loader " + (l.timeCreateLoader - l.timeForceLoad) / 1000 / 1000 + "ms"
						+ ", " + "wanted " + (l.timeCursorWanted - l.timeForceLoad) / 1000 / 1000 + "ms"
						+ ", " + "created " + (l.timeCursorCreated - l.timeForceLoad) / 1000 / 1000 + "ms"
						+ ", " + "delivered " + (l.timeCursorDelivered - l.timeForceLoad) / 1000 / 1000 + "ms"
						+ ", " + "finished " + (l.timeLoadFinished - l.timeForceLoad) / 1000 / 1000 + "ms"
						+ ", " + "reset " + (l.timeLoaderReset - l.timeForceLoad) / 1000 / 1000 + "ms"
						+ "]";
			}
			return "";
		}
	}

	private static class LoadersCursorLoader extends SimpleCursorLoader {
		private final Loaders loaders;
		private final Bundle args;
		private final long timeCreateLoader = System.nanoTime();
		private long timeForceLoad = timeCreateLoader;
		private long timeCursorWanted = timeCreateLoader; // BG entry
		private long timeCursorCreated = timeCreateLoader; // in BG
		private long timeCursorDelivered = timeCreateLoader; // BG exit, on UI
		long timeLoadFinished = timeCreateLoader; // on UI
		long timeLoaderReset = timeCreateLoader; // on UI

		public LoadersCursorLoader(Context context, Loaders loaders, Bundle args) {
			super(context);
			this.loaders = loaders;
			this.args = args;
		}

		@Override public @NonNull Cursor loadInBackground() {
			timeCursorWanted = System.nanoTime();
			try {
				Cursor cursor = loaders.createCursor(getContext(), args != null? args : NO_ARGS);
				timeCursorCreated = System.nanoTime();
				return cursor; // closed in SimpleCursorLoader when the LoaderManager is destroyed
			} catch (RuntimeException ex) {
				throw new IllegalStateException("Cannot create cursor for " + loaders + " with args: " + args, ex);
			}
		}

		@Override public void deliverResult(Cursor cursor) {
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
			if (BuildConfig.DEBUG) {
				return super.toString() + "=" + loaders + "(" + StringerTools.toShortString(args) + ")";
			} else {
				return super.toString();
			}
		}
	}
}
